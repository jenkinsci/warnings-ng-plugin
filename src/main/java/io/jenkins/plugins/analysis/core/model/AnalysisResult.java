package io.jenkins.plugins.analysis.core.model; // NOPMD

import javax.annotation.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Map;
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
import edu.hm.hafner.util.VisibleForTesting;
import io.jenkins.plugins.analysis.core.history.ReferenceProvider;
import io.jenkins.plugins.analysis.core.quality.AnalysisBuild;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.RunAdapter;
import io.jenkins.plugins.analysis.core.quality.StaticAnalysisRun;

import hudson.XmlFile;
import hudson.model.Api;
import hudson.model.Result;
import hudson.model.Run;

/**
 * Stores the results of a static analysis run. This class is capable of storing a reference to the current build.
 * Provides support for persisting the results of the build and loading and saving of issues (all, new, and fixed) and
 * delta computation.
 *
 * @author Ulli Hafner
 */
@ExportedBean
@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveClassLength"})
public class AnalysisResult implements Serializable, StaticAnalysisRun {
    private static final long serialVersionUID = 1110545450292087475L;

    private static final Logger LOGGER = Logger.getLogger(AnalysisResult.class.getName());
    private static final Pattern ISSUES_FILE_NAME = Pattern.compile("issues.xml", Pattern.LITERAL);
    private static final int NO_BUILD = -1;

    private final String id;
    private final String name;

    private transient ReentrantLock lock = new ReentrantLock();
    private transient Run<?, ?> owner;

    /**
     * All outstanding issues: i.e. all issues, that are part of the current and previous report.
     */
    @CheckForNull
    private transient WeakReference<Issues<?>> outstandingIssuesReference;
    /**
     * All new issues: i.e. all issues, that are part of the current report but have not been shown up in the previous
     * report.
     */
    @CheckForNull
    private transient WeakReference<Issues<?>> newIssuesReference;
    /**
     * All fixed issues: i.e. all issues, that are part of the previous report but are not present in the current report
     * anymore.
     */
    @CheckForNull
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

    private final ImmutableList<String> errors;

    /** Determines since which build we have zero warnings. */
    private int noIssuesSinceBuild;
    /** Determines since which build the result is successful. */
    private int successfulSinceBuild;
    /** Reference build number. If not defined then 0 or -1 could be used. */
    private final int referenceBuild;

    /**
     * The build result of the associated plug-in. This result is an additional state that denotes if this plug-in has
     * changed the overall build result.
     */
    private Result overallResult = Result.SUCCESS;

    /**
     * Creates a new instance of {@link AnalysisResult}.
     *
     * @param owner
     *         the current run as owner of this action
     */
    public AnalysisResult(final Run<?, ?> owner, final ReferenceProvider referenceProvider, final String name,
            final Issues<?> issues, final QualityGate qualityGate, final AnalysisResult previousResult) {
        this(owner, referenceProvider, name, issues, qualityGate, true);

        if (issues.isEmpty()) {
            if (previousResult.noIssuesSinceBuild == NO_BUILD) {
                noIssuesSinceBuild = owner.getNumber();
            }
            else {
                noIssuesSinceBuild = previousResult.noIssuesSinceBuild;
            }
        }
        else {
            noIssuesSinceBuild = NO_BUILD;
        }

        if (overallResult == Result.SUCCESS) {
            if (previousResult.overallResult == Result.SUCCESS) {
                successfulSinceBuild = previousResult.successfulSinceBuild;
            }
            else {
                successfulSinceBuild = owner.getNumber();
            }

        }
        else {
            successfulSinceBuild = NO_BUILD;
        }
    }

    /**
     * Creates a new instance of {@link AnalysisResult}.
     *
     * @param owner
     *         the current run as owner of this action
     */
    public AnalysisResult(final Run<?, ?> owner, final ReferenceProvider referenceProvider, final String name,
            final Issues<?> issues, final QualityGate qualityGate) {
        this(owner, referenceProvider, name, issues, qualityGate, true);

        if (issues.isEmpty()) {
            noIssuesSinceBuild = owner.getNumber();
        }
        else {
            noIssuesSinceBuild = NO_BUILD;
        }
        if (overallResult == Result.SUCCESS) {
            successfulSinceBuild = owner.getNumber();
        }
        else {
            successfulSinceBuild = NO_BUILD;
        }
    }

    /**
     * Creates a new instance of {@link AnalysisResult}.
     *
     * @param owner
     *         the current run as owner of this action
     */
    @VisibleForTesting
    protected AnalysisResult(final Run<?, ?> owner, final ReferenceProvider referenceProvider, final String name,
            final Issues<?> issues, final QualityGate qualityGate, final boolean canSerialize) {
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

        overallResult = qualityGate.evaluate(this);
        owner.setResult(overallResult);

        if (canSerialize) {
            serializeAnnotations(outstandingIssues, newIssues, fixedIssues);
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

    @Override
    @Exported
    public int getNoIssuesSinceBuild() {
        return noIssuesSinceBuild;
    }

    @Override
    @Exported
    public int getSuccessfulSinceBuild() {
        return successfulSinceBuild;
    }

    /**
     * Returns whether the static analysis result is successful with respect to the defined {@link QualityGate}.
     *
     * @return {@code true} if the static analysis result is successful, {@code false} if the static analysis result is
     *         {@link Result#UNSTABLE} or {@link Result#FAILURE}
     */
    @Exported
    public boolean isSuccessful() {
        return overallResult == Result.SUCCESS;
    }

    @Override
    @Exported
    public QualityGate getQualityGate() {
        return qualityGate;
    }

    @Override
    @Exported
    public Result getOverallResult() {
        return overallResult;
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
