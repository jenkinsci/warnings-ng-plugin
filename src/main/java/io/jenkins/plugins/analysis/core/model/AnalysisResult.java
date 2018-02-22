package io.jenkins.plugins.analysis.core.model; // NOPMD

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import io.jenkins.plugins.analysis.core.history.ReferenceProvider;
import io.jenkins.plugins.analysis.core.quality.AnalysisBuild;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.RunAdapter;

import hudson.XmlFile;
import hudson.model.Api;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.analysis.core.HealthDescriptor;

/**
 * Stores the results of a static analysis run. This class is capable of storing a reference to the current build.
 * Provides support for persisting the results of the build and loading and saving of issues (all, new, and fixed) and
 * delta computation.
 *
 * @author Ulli Hafner
 */
@ExportedBean
@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveClassLength"})
public class AnalysisResult implements Serializable, StaticAnalysisRun2 {
    private static final long serialVersionUID = 1110545450292087475L;
    private static final Logger LOGGER = Logger.getLogger(AnalysisResult.class.getName());
    private static final Pattern ISSUES_FILE_NAME = Pattern.compile("issues.xml", Pattern.LITERAL);

    private final String id;
    private final String name;

    private transient ReentrantLock lock = new ReentrantLock();
    private transient Run<?, ?> owner;

    /**
     * All outstanding issues: i.e. all issues, that are part of the current and previous report.
     */
    private transient WeakReference<Issues<?>> outstandingIssuesReference;
    /**
     * All new issues: i.e. all issues, that are part of the current report but have not been shown up in the previous
     * report.
     */
    private transient WeakReference<Issues<?>> newIssuesReference;
    /**
     * All fixed issues: i.e. all issues, that are part of the previous report but are not present in the current report
     * anymore.
     */
    private transient WeakReference<Issues<?>> fixedIssuesReference;

    private final QualityGate qualityGate;

    /** The total number of issues in this build. */
    private final int size;
    /** The number of new issues in this build. */
    private final int newSize;
    /** The number of fixed issues in this build. */
    private final int fixedSize;

    /** The number of low priority warnings in this build. */
    private final int lowPrioritySize;
    /** The number of normal priority warnings in this build. */
    private final int normalPrioritySize;
    /** The number of high priority warnings in this build. */
    private final int highPrioritySize;

    /** The number of low priority warnings in this build. */
    private final int newLowPrioritySize;
    /** The number of normal priority warnings in this build. */
    private final int newNormalPrioritySize;
    /** The number of high priority warnings in this build. */
    private final int newHighPrioritySize;

    /** Determines since which build we have zero warnings. */
    private int zeroWarningsSinceBuild;
    /** Determines since which time we have zero warnings. */
    private long zeroWarningsSinceDate;
    /** Determines the zero warnings high score. */
    private long zeroWarningsHighScore;
    /** Determines if the old zero high score has been broken. */
    private boolean isZeroWarningsHighScore;
    /** Determines the number of msec still to go before a new high score is reached. */
    private long highScoreGap;

    /** Error messages. */
    private final ImmutableList<String> errors;

    /**
     * The build result of the associated plug-in. This result is an additional state that denotes if this plug-in has
     * changed the overall build result.
     */
    private Result pluginResult = Result.SUCCESS;
    /** Determines since which build the result is successful. */
    private int successfulSinceBuild;
    /** Determines since which time the result is successful. */
    private long successfulSinceDate;
    /** Determines the successful build result high score. */
    private long successfulHighScore;
    /** Determines if the old successful build result high score has been broken. */
    private boolean isSuccessfulHighScore;
    /** Determines the number of msec still to go before a new high score is reached. */
    private long successfulHighScoreGap;
    /** Determines if this result has touched the successful state. */
    private boolean isSuccessfulStateTouched;

    /** Reference build number. If not defined then 0 or -1 could be used. */
    private final int referenceBuild;

    /**
     * Creates a new instance of {@link AnalysisResult}.
     *
     * @param owner
     *         the current run as owner of this action
     */
    public AnalysisResult(final Run<?, ?> owner, final ReferenceProvider referenceProvider, final String name,
            final Optional<AnalysisResult> previousResult, final QualityGate qualityGate,
            final Issues<?> issues) {
        this(name, owner, referenceProvider, previousResult, qualityGate, issues, true);
    }

    /**
     * Creates a new instance of {@link AnalysisResult}.
     *
     * @param owner
     *         the current run as owner of this action
     */
    // FIXME: should we ignore the issues in previousResult?
    protected AnalysisResult(final String name, final Run<?, ?> owner,
            final ReferenceProvider referenceProvider,
            final Optional<AnalysisResult> previousResult, final QualityGate qualityGate,
            final Issues<?> issues, final boolean canSerialize) {
        this.name = name;
        this.owner = owner;
        this.qualityGate = qualityGate;

        errors = issues.getErrorMessages();

        id = issues.getId();
        size = issues.getSize();
        highPrioritySize = issues.getHighPrioritySize();
        normalPrioritySize = issues.getNormalPrioritySize();
        lowPrioritySize = issues.getLowPrioritySize();

        referenceBuild = referenceProvider.getNumber();

        Issues<?> referenceResult = referenceProvider.getIssues();
        IssueDifference difference = new IssueDifference(issues, this.owner.getNumber(), referenceResult);

        Issues<?> outstandingIssues = difference.getOutstandingIssues();
        outstandingIssuesReference = new WeakReference<>(outstandingIssues);

        Issues<?> newIssues = difference.getNewIssues();
        newIssuesReference = new WeakReference<>(newIssues);
        newSize = newIssues.getSize();
        newHighPrioritySize = newIssues.getHighPrioritySize();
        newNormalPrioritySize = newIssues.getNormalPrioritySize();
        newLowPrioritySize = newIssues.getLowPrioritySize();

        Issues<?> fixedIssues = difference.getFixedIssues();
        fixedIssuesReference = new WeakReference<>(fixedIssues);
        fixedSize = fixedIssues.size();

        computeZeroWarningsHighScore(owner, issues, previousResult, issues.isEmpty());

        evaluateStatus(previousResult);

        if (canSerialize) {
            serializeAnnotations(outstandingIssues, newIssues, fixedIssues);
        }
    }

    /**
     * Computes the zero warnings high score based on the current build and the previous build (with results of the
     * associated plug-in).
     *
     * @param build
     *         the current build
     */
    private void computeZeroWarningsHighScore(final Run<?, ?> build, final Issues<?> currentResult,
            final Optional<AnalysisResult> previousResult, final boolean containsIssues) {
        if (previousResult.isPresent()) {
            AnalysisResult previous = previousResult.get();
            if (containsIssues) {
                if (previous.getTotalSize() == 0) {
                    zeroWarningsSinceBuild = previous.getZeroWarningsSinceBuild();
                    zeroWarningsSinceDate = previous.getZeroWarningsSinceDate();
                }
                else {
                    zeroWarningsSinceBuild = build.getNumber();
                    zeroWarningsSinceDate = build.getTimestamp().getTimeInMillis();
                }
                zeroWarningsHighScore = Math.max(previous.getZeroWarningsHighScore(),
                        build.getTimestamp().getTimeInMillis() - zeroWarningsSinceDate);
                if (previous.getZeroWarningsHighScore() == 0) {
                    isZeroWarningsHighScore = true;
                }
                else {
                    isZeroWarningsHighScore = zeroWarningsHighScore != previous.getZeroWarningsHighScore();

                }
                if (!isZeroWarningsHighScore) {
                    highScoreGap = previous.getZeroWarningsHighScore()
                            - (build.getTimestamp().getTimeInMillis() - zeroWarningsSinceDate);
                }
            }
            else {
                zeroWarningsHighScore = previous.getZeroWarningsHighScore();
            }
        }
        else {
            if (currentResult.isEmpty()) {
                zeroWarningsSinceBuild = build.getNumber();
                zeroWarningsSinceDate = build.getTimestamp().getTimeInMillis();
                isZeroWarningsHighScore = true;
                zeroWarningsHighScore = 0;
            }
        }
    }

    /**
     * Updates the build status, i.e. sets this plug-in result status field to the corresponding {@link Result}.
     * Additionally, the {@link Result} of the build that owns this instance of {@link AnalysisResult} will be also
     * changed.
     */
    private void evaluateStatus(final Optional<AnalysisResult> previousResult) {
        // FIXME split two parts
        Result result = qualityGate.evaluate(this);
        pluginResult = result;
        owner.setResult(pluginResult);

        // FIXME is this still required?
        isSuccessfulStateTouched = true;

        if (previousResult.isPresent()) {
            AnalysisResult previous = previousResult.get();
            // FIXME: same code to compute zero warnings
            if (isSuccessful()) {
                if (previous.isSuccessful() && previous.isSuccessfulTouched()) {
                    successfulSinceBuild = previous.getSuccessfulSinceBuild();
                    successfulSinceDate = previous.getSuccessfulSinceDate();
                }
                else {
                    successfulSinceBuild = owner.getNumber();
                    successfulSinceDate = owner.getTimestamp().getTimeInMillis();
                }
                successfulHighScore = Math.max(previous.getSuccessfulHighScore(),
                        owner.getTimestamp().getTimeInMillis() - successfulSinceDate);
                if (previous.getSuccessfulHighScore() == 0) {
                    isSuccessfulHighScore = true;
                }
                else {
                    isSuccessfulHighScore = successfulHighScore != previous.getSuccessfulHighScore();

                }
                if (!isSuccessfulHighScore) {
                    successfulHighScoreGap = previous.getSuccessfulHighScore()
                            - (owner.getTimestamp().getTimeInMillis() - successfulSinceDate);
                }
            }
            else {
                successfulHighScore = previous.getSuccessfulHighScore();
            }
        }
        else {
            if (isSuccessful()) {
                resetSuccessfulState();
            }
        }
    }

    /**
     * Returns the ID of the static analysis result.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns an optional user provided name for this result. If left empty, the predefined name of the {@link
     * StaticAnalysisLabelProvider} is used.
     *
     * @return optional name
     */
    public String getName() {
        return StringUtils.defaultString(name);
    }

    /**
     * Returns the run as owner of this action.
     *
     * @return the owner
     */
    public Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Sets the run for this result after Jenkins read its data from disk.
     *
     * @param owner
     *         the initialized run
     */
    public void setOwner(final Run<?, ?> owner) {
        this.owner = owner;
        lock = new ReentrantLock();
    }

    /**
     * Returns the serialization file for the fixed warnings.
     *
     * @param suffix
     *         suffix of the file
     *
     * @return the serialization file.
     */
    private XmlFile getDataFile(final String suffix) {
        return new XmlFile(new IssueStream().createStream(), new File(getOwner().getRootDir(),
                ISSUES_FILE_NAME.matcher(getSerializationFileName())
                        .replaceAll(Matcher.quoteReplacement(suffix + "-issues.xml"))));
    }

    private String getSerializationFileName() {
        return id + "-issues.xml";
    }

    private void serializeAnnotations(final Issues<?> outstandingIssues,
            final Issues<?> newIssues, final Issues<?> fixedIssues) {
        serializeIssues(outstandingIssues, "outstanding");
        serializeIssues(newIssues, "new");
        serializeIssues(fixedIssues, "fixed");
    }

    private void serializeIssues(final Issues<?> issues, final String suffix) {
        try {
            getDataFile(suffix).write(issues);
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, String.format("Failed to serialize the %s issues of the build.", suffix),
                    exception);
        }
    }

    /**
     * Returns all issues of the associated static analysis run. These include outstanding issues as well as new
     * issues.
     *
     * @return all issues
     */
    public Issues<?> getIssues() {
        Issues<Issue> merged = new Issues<>();
        merged.addAll(getNewIssues(), getOutstandingIssues());
        return merged;
    }

    /**
     * Returns all outstanding issues of the associated static analysis run. I.e. all issues, that are part of the
     * current and previous report.
     *
     * @return all outstanding issues
     */
    public Issues<?> getOutstandingIssues() {
        return getIssues(AnalysisResult::getOutstandingIssuesReference, AnalysisResult::setOutstandingIssuesReference,
                "outstanding");
    }

    /**
     * Returns all new issues of the associated static analysis run. I.e. all issues, that are part of the current
     * report but have not been shown up in the previous report.
     *
     * @return all new issues
     */
    public Issues<?> getNewIssues() {
        return getIssues(AnalysisResult::getNewIssuesReference, AnalysisResult::setNewIssuesReference,
                "new");
    }

    /**
     * Returns all fixed issues of the associated static analysis run. I.e. all issues, that are part of the previous
     * report but are not present in the current report anymore.
     *
     * @return all fixed issues
     */
    public Issues<?> getFixedIssues() {
        return getIssues(AnalysisResult::getFixedIssuesReference, AnalysisResult::setFixedIssuesReference,
                "fixed");
    }

    private WeakReference<Issues<?>> getOutstandingIssuesReference() {
        return outstandingIssuesReference;
    }

    private void setOutstandingIssuesReference(final WeakReference<Issues<?>> outstandingIssuesReference) {
        this.outstandingIssuesReference = outstandingIssuesReference;
    }

    private WeakReference<Issues<?>> getNewIssuesReference() {
        return newIssuesReference;
    }

    private void setNewIssuesReference(final WeakReference<Issues<?>> newIssuesReference) {
        this.newIssuesReference = newIssuesReference;
    }

    private WeakReference<Issues<?>> getFixedIssuesReference() {
        return fixedIssuesReference;
    }

    private void setFixedIssuesReference(final WeakReference<Issues<?>> fixedIssuesReference) {
        this.fixedIssuesReference = fixedIssuesReference;
    }

    private Issues<?> getIssues(final Function<AnalysisResult, WeakReference<Issues<?>>> getter,
            final BiConsumer<AnalysisResult, WeakReference<Issues<?>>> setter, final String suffix) {
        lock.lock();
        try {
            if (getter.apply(this) == null) {
                return readIssues(setter, suffix);
            }
            Issues<?> result = getter.apply(this).get();
            if (result == null) {
                return readIssues(setter, suffix);
            }
            return result;
        }
        finally {
            lock.unlock();
        }
    }

    private Issues<?> readIssues(final BiConsumer<AnalysisResult, WeakReference<Issues<?>>> setter,
            final String suffix) {
        Issues<?> issues = readIssues(suffix);
        setter.accept(this, new WeakReference<>(issues));
        return issues;
    }

    private Issues<?> readIssues(final String suffix) {
        XmlFile dataFile = getDataFile(suffix);
        try {
            Object deserialized = dataFile.read();
            
            if (deserialized instanceof Issues) {
                Issues<?> result = (Issues<?>) deserialized;

                LOGGER.log(Level.FINE, "Loaded data file " + dataFile + " for run " + getOwner());

                return result;
            }
            LOGGER.log(Level.SEVERE, "Failed to load " + dataFile + ", wrong type: " + deserialized);
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to load " + dataFile, exception);

        }
        return new Issues<>(); // fallback
    }

    /**
     * Gets the remote API for this build result.
     *
     * @return the remote API
     */
    public Api getApi() {
        return new Api(this);
    }

    /**
     * Returns the number of the run since we have zero warnings.
     *
     * @return the number of the run since we have zero warnings
     */
    @Override
    @Exported
    public int getZeroWarningsSinceBuild() {
        return zeroWarningsSinceBuild;
    }

    /**
     * Returns the time since we have zero warnings.
     *
     * @return the time since we have zero warnings
     */
    @Exported
    public long getZeroWarningsSinceDate() {
        return zeroWarningsSinceDate;
    }

    /**
     * Returns the maximum period with zero warnings in a build.
     *
     * @return the time since we have zero warnings
     */
    @Override
    @Exported
    public long getZeroWarningsHighScore() {
        return zeroWarningsHighScore;
    }

    /**
     * Returns if the current result reached the old zero warnings high score.
     *
     * @return {@code true}, if the current result reached the old zero warnings high score.
     */
    @Override
    @Exported
    public boolean isNewZeroWarningsHighScore() {
        return isZeroWarningsHighScore;
    }

    /**
     * Returns the number of msec still to go before a new high score is reached.
     *
     * @return the number of msec still to go before a new high score is reached.
     */
    @Override
    public long getHighScoreGap() {
        return highScoreGap;
    }

    /**
     * Returns the build since we are successful.
     *
     * @return the build since we are successful
     */
    @Exported
    public int getSuccessfulSinceBuild() {
        return successfulSinceBuild;
    }

    /**
     * Returns the time since we are successful.
     *
     * @return the time since we are successful
     */
    @Exported
    public long getSuccessfulSinceDate() {
        return successfulSinceDate;
    }

    /**
     * Returns the maximum period of successful builds.
     *
     * @return the maximum period of successful builds
     */
    @Override
    @Exported
    public long getSuccessfulHighScore() {
        return successfulHighScore;
    }

    /**
     * Returns if the current result reached the old successful high score.
     *
     * @return {@code true}, if the current result reached the old successful high score.
     */
    @Override
    @Exported
    public boolean isNewSuccessfulHighScore() {
        return isSuccessfulHighScore;
    }

    /**
     * Returns the number of msec still to go before a new highscore is reached.
     *
     * @return the number of msec still to go before a new highscore is reached.
     */
    @Override
    public long getSuccessfulHighScoreGap() {
        return successfulHighScoreGap;
    }

    /**
     * Returns whether this build is successful with respect to the {@link HealthDescriptor} of this result.
     *
     * @return {@code true} if the build is successful, {@code false} if the build has been set to {@link
     *         Result#UNSTABLE} or {@link Result#FAILURE} by this result.
     */
    @Override
    public boolean isSuccessful() {
        return pluginResult == Result.SUCCESS;
    }

    /**
     * Returns the {@link Result} of the plug-in.
     *
     * @return the plugin result
     */
    @Override
    @Exported
    public Result getPluginResult() {
        return pluginResult;
    }

    /**
     * Returns whether the successful state has been touched.
     *
     * @return {@code true} if the successful state has been touched, {@code false} otherwise
     */
    @Override
    public boolean isSuccessfulTouched() {
        return isSuccessfulStateTouched;
    }

    /**
     * /** Resets the successful high score counters.
     */
    private void resetSuccessfulState() {
        successfulSinceBuild = owner.getNumber();
        successfulSinceDate = owner.getTimestamp().getTimeInMillis();
        isSuccessfulHighScore = true;
        successfulHighScore = 0;
    }

    /**
     * Returns the summary message for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getSummary() {
        return getTool().getSummary(size, getIssues().getModules().size());
    }

    /**
     * Returns the detail messages for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getDetails() {
        return new Summary(id, name, this).toString();
    }

    /**
     * Returns the header for the build result page.
     *
     * @return the header for the build result page
     */
    public String getHeader() {
        return getTool().getLinkName();
    }

    @Override
    public String toString() {
        return getDisplayName() + " : " + getTotalSize() + " issues";
    }

    private StaticAnalysisLabelProvider getTool() {
        return new LabelProviderFactory().create(id, name);
    }

    public String getDisplayName() {
        return getTool().getLinkName();
    }

    @Override
    public String getReason() {
        // TODO: use this code directly?
        return qualityGate.evaluate(this).toString();
    }

    @Override
    public int getReferenceBuild() {
        return referenceBuild;
    }

    @Override
    public Map<String, Integer> getSizePerOrigin() {
        return getIssues().getPropertyCount(issue -> issue.getOrigin());
    }

    @Override
    public AnalysisBuild getBuild() {
        return new RunAdapter(owner);
    }

    @Override
    public int getTotalSize() {
        return size;
    }

    @Override
    public int getTotalSize(final Priority priority) {
        if (priority == Priority.HIGH) {
            return getTotalHighPrioritySize();
        }
        if (priority == Priority.NORMAL) {
            return getTotalNormalPrioritySize();
        }
        if (priority == Priority.LOW) {
            return getTotalLowPrioritySize();
        }
        return 0;
    }

    @Override
    public int getTotalHighPrioritySize() {
        return highPrioritySize;
    }

    @Override
    public int getTotalNormalPrioritySize() {
        return normalPrioritySize;
    }

    @Override
    public int getTotalLowPrioritySize() {
        return lowPrioritySize;
    }

    @Override
    public int getNewSize() {
        return newSize;
    }

    @Override
    public int getNewHighPrioritySize() {
        return newHighPrioritySize;
    }

    @Override
    public int getNewNormalPrioritySize() {
        return newNormalPrioritySize;
    }

    @Override
    public int getNewLowPrioritySize() {
        return newLowPrioritySize;
    }

    @Override
    public int getFixedSize() {
        return fixedSize;
    }
}
