package io.jenkins.plugins.analysis.core.model; // NOPMD

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.history.ReferenceProvider;
import io.jenkins.plugins.analysis.core.quality.AnalysisBuild;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.quality.RunAdapter;

import hudson.XmlFile;
import hudson.model.Result;
import hudson.model.Run;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.util.VisibleForTesting;

/**
 * Stores the results of a static analysis run. This class is capable of storing a reference to the current build.
 * Provides support for persisting the results of the build and loading and saving of issues (all, new, and fixed) and
 * delta computation.
 *
 * @author Ulli Hafner
 */
@ExportedBean
@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveClassLength"})
public class AnalysisResult implements Serializable {
    private static final long serialVersionUID = 1110545450292087475L;

    private static final Logger LOGGER = Logger.getLogger(AnalysisResult.class.getName());
    private static final Pattern ISSUES_FILE_NAME = Pattern.compile("issues.xml", Pattern.LITERAL);
    private static final int NO_BUILD = -1;
    private static final String NO_REFERENCE = StringUtils.EMPTY;

    private final String id;

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

    private final int size;
    private final int newSize;
    private final int fixedSize;

    private final int lowPrioritySize;
    private final int normalPrioritySize;
    private final int highPrioritySize;

    private final int newLowPrioritySize;
    private final int newNormalPrioritySize;
    private final int newHighPrioritySize;

    private final Map<String, Integer> sizePerOrigin;

    private final ImmutableList<String> errors;
    private final ImmutableList<String> infos;

    /** Determines since which build we have zero warnings. */
    private int noIssuesSinceBuild;
    /** Determines since which build the result is successful. */
    private int successfulSinceBuild;
    /**
     * Reference run to compute the issues difference:
     * since a run could not be persisted directly, the IDs are only stored.
     */
    private final String referenceJob;
    private final String referenceBuild;


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
     * @param referenceProvider
     *         provides the reference build
     * @param issues
     *         the issues of this result
     * @param qualityGate
     *         the quality gate to enforce
     * @param previousResult
     *         the analysis result of the previous run
     */
    public AnalysisResult(final Run<?, ?> owner, final ReferenceProvider referenceProvider,
            final Issues<?> issues, final QualityGate qualityGate, final AnalysisResult previousResult) {
        this(owner, referenceProvider, issues, qualityGate, true);

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
     * @param referenceProvider
     *         provides the reference build
     * @param issues
     *         the issues of this result
     * @param qualityGate
     *         the quality gate to enforce
     */
    public AnalysisResult(final Run<?, ?> owner, final ReferenceProvider referenceProvider,
            final Issues<?> issues, final QualityGate qualityGate) {
        this(owner, referenceProvider, issues, qualityGate, true);

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
     * @param referenceProvider
     *         provides the reference build
     * @param issues
     *         the issues of this result
     * @param qualityGate
     *         the quality gate to enforce
     * @param canSerialize
     *         determines whether the result should be persisted in the build folder
     */
    @VisibleForTesting
    protected AnalysisResult(final Run<?, ?> owner, final ReferenceProvider referenceProvider,
            final Issues<?> issues, final QualityGate qualityGate, final boolean canSerialize) {
        this.owner = owner;
        this.qualityGate = qualityGate;

        id = issues.getId();
        size = issues.getSize();
        highPrioritySize = issues.getHighPrioritySize();
        normalPrioritySize = issues.getNormalPrioritySize();
        lowPrioritySize = issues.getLowPrioritySize();

        Optional<Run<?, ?>> run = referenceProvider.getAnalysisRun();
        if (run.isPresent()) {
            Run<?, ?> referenceRun = run.get();
            referenceJob = referenceRun.getParent().getFullName();
            referenceBuild = referenceRun.getId();
        }
        else {
            referenceJob = NO_REFERENCE;
            referenceBuild = StringUtils.EMPTY;
        }
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

        List<String> messages = new ArrayList<>(issues.getInfoMessages().castToList());

        if (qualityGate.isEnabled()) {
            QualityGateResult result = qualityGate.evaluate(this);
            overallResult = result.getOverallResult();
            if (overallResult.isBetterOrEqualTo(Result.SUCCESS)) {
                messages.add("All quality gates have been passed");
            }
            else {
                messages.add(String.format("Some quality gates have been missed: overall result is %s", overallResult));
                result.getEvaluations(this, qualityGate).forEach(message -> messages.add(message));
            }
            owner.setResult(overallResult);
        }
        else {
            messages.add("No quality gates have been set - skipping");
            overallResult = Result.SUCCESS;
        }

        infos = Lists.immutable.withAll(messages);
        errors = issues.getErrorMessages();

        sizePerOrigin = issues.getPropertyCount(issue -> issue.getOrigin());

        if (canSerialize) {
            serializeAnnotations(outstandingIssues, newIssues, fixedIssues);
        }
    }

    /**
     * Returns the ID of the static analysis result.
     *
     * @return the ID
     */
    @Exported
    public String getId() {
        return id;
    }

    /**
     * Returns the run that created this static analysis result.
     *
     * @return the run
     */
    @Exported
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
     * Returns the error messages of the analysis run.
     *
     * @return the error messages
     */
    @Exported
    public ImmutableList<String> getErrorMessages() {
        return errors;
    }

    /**
     * Returns the info messages of the analysis run.
     *
     * @return the info messages
     */
    @Exported
    public ImmutableList<String> getInfoMessages() {
        return infos;
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
    @Exported
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
    @Exported
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
    @Exported
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
    @Exported
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
     * Returns the build number since the associated job has no issues.
     *
     * @return the build number since there are no issues, or -1 if issues have been reported
     */
    public int getNoIssuesSinceBuild() {
        return noIssuesSinceBuild;
    }

    /**
     * Returns the build number since the associated job has a successful static analysis result.
     *
     * @return the build number since the static analysis result is successful, or -1 if the result is not successful
     */
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

    public QualityGate getQualityGate() {
        return qualityGate;
    }

    /**
     * Returns the {@link Result} of the static analysis run.
     *
     * @return the static analysis result
     */
    @Exported
    public Result getOverallResult() {
        return overallResult;
    }

    @Override
    public String toString() {
        return getId() + " : " + getTotalSize() + " issues";
    }

    /**
     * Returns the reference static analysis run that has been used to compute the new issues.
     *
     * @return the reference build
     */
    public Optional<Run<?, ?>> getReferenceBuild() {
        if (referenceJob == NO_REFERENCE) {
            return Optional.empty();
        }
        return new JenkinsFacade().getBuild(referenceJob, referenceBuild);
    }

    /**
     * Returns the number of issues in this analysis run, mapped by their origin.
     *
     * @return number of issues per origin
     */
    public Map<String, Integer> getSizePerOrigin() {
        return Maps.immutable.ofAll(sizePerOrigin).toMap();
    }

    /**
     * Returns the associated build that this run was part of.
     *
     * @return the associated build
     */
    public AnalysisBuild getBuild() {
        return new RunAdapter(owner);
    }

    /**
     * Returns the total number of issues in this analysis run.
     *
     * @return total number of issues
     */
    @Exported
    public int getTotalSize() {
        return size;
    }

    /**
     * Returns the total number of issues in this analysis run, that have the specified {@link Priority}.
     *
     * @param priority
     *         the priority of the issues to match
     *
     * @return total number of issues
     */
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

    /**
     * Returns the total number of high priority issues in this analysis run.
     *
     * @return total number of high priority issues
     */
    @Exported
    public int getTotalHighPrioritySize() {
        return highPrioritySize;
    }

    /**
     * Returns the total number of normal priority issues in this analysis run.
     *
     * @return total number of normal priority issues
     */
    @Exported
    public int getTotalNormalPrioritySize() {
        return normalPrioritySize;
    }

    /**
     * Returns the total number of low priority issues in this analysis run.
     *
     * @return total number of low priority of issues
     */
    @Exported
    public int getTotalLowPrioritySize() {
        return lowPrioritySize;
    }

    /**
     * Returns the number of new issues in this analysis run.
     *
     * @return number of new issues
     */
    @Exported
    public int getNewSize() {
        return newSize;
    }

    /**
     * Returns the number of new high priority issues in this analysis run.
     *
     * @return number of new high priority issues
     */
    @Exported
    public int getNewHighPrioritySize() {
        return newHighPrioritySize;
    }

    /**
     * Returns the number of new normal priority issues in this analysis run.
     *
     * @return number of new normal priority issues
     */
    @Exported
    public int getNewNormalPrioritySize() {
        return newNormalPrioritySize;
    }

    /**
     * Returns the number of new low priority issues in this analysis run.
     *
     * @return number of new low priority of issues
     */
    @Exported
    public int getNewLowPrioritySize() {
        return newLowPrioritySize;
    }

    /**
     * Returns the number of fixed issues in this analysis run.
     *
     * @return number of fixed issues
     */
    @Exported
    public int getFixedSize() {
        return fixedSize;
    }
}
