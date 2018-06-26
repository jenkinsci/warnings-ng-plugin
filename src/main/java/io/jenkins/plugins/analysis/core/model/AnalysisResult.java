package io.jenkins.plugins.analysis.core.model; // NOPMD

import javax.annotation.CheckForNull;
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory;
import io.jenkins.plugins.analysis.core.quality.AnalysisBuild;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.quality.RunAdapter;
import io.jenkins.plugins.analysis.core.quality.Status;

import hudson.XmlFile;
import hudson.model.Result;
import hudson.model.Run;

/**
 * Stores the results of a static analysis run. Provides support for persisting the results of the build and loading and
 * saving of issues (all, new, and fixed) and delta computation.
 *
 * @author Ulli Hafner
 */
@ExportedBean
@SuppressFBWarnings(value = "SE", justification = "transient fields are restored using a Jenkins callback (or are checked for null)")
@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveClassLength"})
public class AnalysisResult implements Serializable {
    private static final long serialVersionUID = 1110545450292087475L;

    private static final Logger LOGGER = Logger.getLogger(AnalysisResult.class.getName());
    private static final Pattern ISSUES_FILE_NAME = Pattern.compile("issues.xml", Pattern.LITERAL);
    private static final int NO_BUILD = -1;
    private static final String NO_REFERENCE = StringUtils.EMPTY;
    private static final Report EMPTY_REPORT = new Report();

    private final String id;
    private final QualityGate qualityGate;
    private final int size;
    private final int newSize;
    private final int fixedSize;
    private final Map<String, Integer> sizePerOrigin;
    private final Map<Severity, Integer> sizePerSeverity;
    private final Map<Severity, Integer> newSizePerSeverity;
    private final List<String> errors;
    private final List<String> messages;
    /**
     * Reference run to compute the issues difference: since a run could not be persisted directly, the IDs are only
     * stored.
     */
    private final String referenceJob;
    private final String referenceBuild;
    private transient ReentrantLock lock = new ReentrantLock();
    private transient Run<?, ?> owner;
    /**
     * All outstanding issues: i.e. all issues, that are part of the current and reference report.
     */
    @CheckForNull
    private transient WeakReference<Report> outstandingIssuesReference;
    /**
     * All new issues: i.e. all issues, that are part of the current report but have not been shown up in the reference
     * report.
     */
    @CheckForNull
    private transient WeakReference<Report> newIssuesReference;
    /**
     * All fixed issues: i.e. all issues, that are part of the reference report but are not present in the current
     * report anymore.
     */
    @CheckForNull
    private transient WeakReference<Report> fixedIssuesReference;
    /** Determines since which build we have zero warnings. */
    private int noIssuesSinceBuild;
    /** Determines since which build the result is successful. */
    private int successfulSinceBuild;
    /** The result of the quality gate evaluation. */
    private Status status = Status.PASSED;

    /**
     * Creates a new instance of {@link AnalysisResult}.
     *
     * @param owner
     *         the current build as owner of this action
     * @param history
     *         provides the reference build
     * @param report
     *         the issues of this result
     * @param qualityGate
     *         the quality gate to enforce
     * @param previousResult
     *         the analysis result of the previous run
     */
    public AnalysisResult(final Run<?, ?> owner, final AnalysisHistory history,
            final Report report, final QualityGate qualityGate, final AnalysisResult previousResult) {
        this(owner, history, report, qualityGate, true);

        if (report.isEmpty()) {
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

        if (status == Status.PASSED) {
            if (previousResult.status == Status.PASSED) {
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
     *         the current build as owner of this action
     * @param history
     *         provides the reference build
     * @param report
     *         the issues of this result
     * @param qualityGate
     *         the quality gate to enforce
     */
    public AnalysisResult(final Run<?, ?> owner, final AnalysisHistory history,
            final Report report, final QualityGate qualityGate) {
        this(owner, history, report, qualityGate, true);

        if (report.isEmpty()) {
            noIssuesSinceBuild = owner.getNumber();
        }
        else {
            noIssuesSinceBuild = NO_BUILD;
        }
        if (status == Status.PASSED) {
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
     * @param history
     *         provides the reference build
     * @param report
     *         the issues of this result
     * @param qualityGate
     *         the quality gate to enforce
     * @param canSerialize
     *         determines whether the result should be persisted in the build folder
     */
    @VisibleForTesting
    protected AnalysisResult(final Run<?, ?> owner, final AnalysisHistory history,
            final Report report, final QualityGate qualityGate, final boolean canSerialize) {
        this.owner = owner;
        this.qualityGate = qualityGate;

        id = report.getOrigin();

        size = report.getSize();
        sizePerOrigin = report.getPropertyCount(Issue::getOrigin);
        sizePerSeverity = report.getPropertyCount(Issue::getSeverity);

        Report outstandingIssues;
        Report newIssues;
        Report fixedIssues;

        // TODO: Compute reference outside in new class and add result to report logging
        Optional<Run<?, ?>> run = history.getPreviousBuild();
        if (run.isPresent()) {
            Run<?, ?> referenceRun = run.get();
            referenceJob = referenceRun.getParent().getFullName();
            referenceBuild = referenceRun.getId();

            IssueDifference difference = new IssueDifference(report, this.owner.getNumber(),
                    history.getPreviousIssues());

            outstandingIssues = difference.getOutstandingIssues();
            newIssues = difference.getNewIssues();
            fixedIssues = difference.getFixedIssues();
        }
        else {
            referenceJob = NO_REFERENCE;
            referenceBuild = StringUtils.EMPTY;

            outstandingIssues = report;
            newIssues = EMPTY_REPORT;
            fixedIssues = EMPTY_REPORT;
        }

        outstandingIssuesReference = new WeakReference<>(outstandingIssues);

        newIssuesReference = new WeakReference<>(newIssues);
        newSize = newIssues.getSize();
        newSizePerSeverity = newIssues.getPropertyCount(Issue::getSeverity);

        fixedIssuesReference = new WeakReference<>(fixedIssues);
        fixedSize = fixedIssues.size();

        List<String> aggregatedMessages = new ArrayList<>(report.getInfoMessages().castToList());

        if (qualityGate.isEnabled()) {
            QualityGateResult result = qualityGate.evaluate(this);
            status = result.getStatus();
            if (status.isSuccessful()) {
                aggregatedMessages.add("All quality gates have been passed");
            }
            else {
                aggregatedMessages.add(
                        String.format("Some quality gates have been missed: overall result is %s", status));
                aggregatedMessages.addAll(result.getEvaluations(this, qualityGate));
            }
            owner.setResult(createResult());
        }
        else {
            aggregatedMessages.add("No quality gates have been set - skipping");
            status = Status.INACTIVE;
        }

        this.messages = new ArrayList<>(aggregatedMessages);
        errors = new ArrayList<>(report.getErrorMessages().castToList());

        if (canSerialize) {
            serializeAnnotations(outstandingIssues, newIssues, fixedIssues);
        }
    }

    private Result createResult() {
        if (status == Status.WARNING) {
            return Result.UNSTABLE;
        }
        if (status == Status.FAILED) {
            return Result.FAILURE;
        }
        return Result.SUCCESS;
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
        return Lists.immutable.withAll(errors);
    }

    /**
     * Returns the info messages of the analysis run.
     *
     * @return the info messages
     */
    @Exported
    public ImmutableList<String> getInfoMessages() {
        return Lists.immutable.withAll(messages);
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

    private void serializeAnnotations(final Report outstandingIssues,
            final Report newIssues, final Report fixedIssues) {
        serializeIssues(outstandingIssues, "outstanding");
        serializeIssues(newIssues, "new");
        serializeIssues(fixedIssues, "fixed");
    }

    private void serializeIssues(final Report report, final String suffix) {
        try {
            getDataFile(suffix).write(report);
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
    public Report getIssues() {
        Report merged = new Report();
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
    public Report getOutstandingIssues() {
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
    public Report getNewIssues() {
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
    public Report getFixedIssues() {
        return getIssues(AnalysisResult::getFixedIssuesReference, AnalysisResult::setFixedIssuesReference,
                "fixed");
    }

    private WeakReference<Report> getOutstandingIssuesReference() {
        return outstandingIssuesReference;
    }

    private void setOutstandingIssuesReference(final WeakReference<Report> outstandingIssuesReference) {
        this.outstandingIssuesReference = outstandingIssuesReference;
    }

    private WeakReference<Report> getNewIssuesReference() {
        return newIssuesReference;
    }

    private void setNewIssuesReference(final WeakReference<Report> newIssuesReference) {
        this.newIssuesReference = newIssuesReference;
    }

    private WeakReference<Report> getFixedIssuesReference() {
        return fixedIssuesReference;
    }

    private void setFixedIssuesReference(final WeakReference<Report> fixedIssuesReference) {
        this.fixedIssuesReference = fixedIssuesReference;
    }

    private Report getIssues(final Function<AnalysisResult, WeakReference<Report>> getter,
            final BiConsumer<AnalysisResult, WeakReference<Report>> setter, final String suffix) {
        lock.lock();
        try {
            if (getter.apply(this) == null) {
                return readIssues(setter, suffix);
            }
            Report result = getter.apply(this).get();
            if (result == null) {
                return readIssues(setter, suffix);
            }
            return result;
        }
        finally {
            lock.unlock();
        }
    }

    private Report readIssues(final BiConsumer<AnalysisResult, WeakReference<Report>> setter,
            final String suffix) {
        Report report = readIssues(suffix);
        setter.accept(this, new WeakReference<>(report));
        return report;
    }

    private Report readIssues(final String suffix) {
        XmlFile dataFile = getDataFile(suffix);
        try {
            Object deserialized = dataFile.read();

            if (deserialized instanceof Report) {
                Report result = (Report) deserialized;

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Loaded data file " + dataFile + " for run " + getOwner());
                }
                return result;
            }
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Failed to load " + dataFile + ", wrong type: " + deserialized);
            }
        }
        catch (IOException exception) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Failed to load " + dataFile, exception);
            }
        }
        return new Report(); // fallback
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
     *         {@link Status#WARNING} or {@link Status#FAILED}
     */
    @Exported
    public boolean isSuccessful() {
        return status.isSuccessful();
    }

    public QualityGate getQualityGate() {
        return qualityGate;
    }

    /**
     * Returns the {@link Status} of the {@link QualityGate} evaluation of the static analysis run.
     *
     * @return the quality gate status
     */
    @Exported
    public Status getStatus() {
        return status;
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
        if (NO_REFERENCE.equals(referenceJob)) {
            return Optional.empty();
        }
        return new JenkinsFacade().getBuild(referenceJob, referenceBuild);
    }

    /**
     * Returns the number of issues in this analysis run, mapped by their origin. The origin is the tool that created
     * the report.
     *
     * @return number of issues per origin
     */
    public Map<String, Integer> getSizePerOrigin() {
        return Maps.immutable.ofAll(sizePerOrigin).toMap();
    }

    /**
     * Returns the number of issues in this analysis run, mapped by {@link Severity}.
     *
     * @return number of issues per priority
     */
    public Map<Severity, Integer> getSizePerSeverity() {
        return Maps.immutable.ofAll(sizePerSeverity).toMap();
    }

    /**
     * Returns the new number of issues in this analysis run, mapped by {@link Severity}.
     *
     * @return number of issues per priority
     */
    public Map<Severity, Integer> getNewSizePerSeverity() {
        return Maps.immutable.ofAll(sizePerSeverity).toMap();
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
     * Returns the total number of issues in this analysis run that have the specified {@link Severity}.
     *
     * @param severity
     *         the severity of the issues to match
     *
     * @return total number of issues
     */
    public int getTotalSizeOf(final Severity severity) {
        return sizePerSeverity.getOrDefault(severity, 0);
    }

    /**
     * Returns the total number of errors in this analysis run.
     *
     * @return total number of errors
     */
    @Exported
    public int getTotalErrorsSize() {
        return getTotalSizeOf(Severity.ERROR);
    }

    /**
     * Returns the total number of high priority issues in this analysis run.
     *
     * @return total number of high priority issues
     */
    @Exported
    public int getTotalHighPrioritySize() {
        return getTotalSizeOf(Severity.WARNING_HIGH);
    }

    /**
     * Returns the total number of normal priority issues in this analysis run.
     *
     * @return total number of normal priority issues
     */
    @Exported
    public int getTotalNormalPrioritySize() {
        return getTotalSizeOf(Severity.WARNING_NORMAL);
    }

    /**
     * Returns the total number of low priority issues in this analysis run.
     *
     * @return total number of low priority of issues
     */
    @Exported
    public int getTotalLowPrioritySize() {
        return getTotalSizeOf(Severity.WARNING_LOW);
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
     * Returns the new number of issues in this analysis run that have the specified {@link Severity}.
     *
     * @param severity
     *         the severity of the issues to match
     *
     * @return total number of issues
     */
    public int getNewSizeOf(final Severity severity) {
        return newSizePerSeverity.getOrDefault(severity, 0);
    }

    /**
     * Returns the number of new errors in this analysis run.
     *
     * @return number of new errors issues
     */
    @Exported
    public int getNewErrorSize() {
        return getNewSizeOf(Severity.ERROR);
    }

    /**
     * Returns the number of new high priority issues in this analysis run.
     *
     * @return number of new high priority issues
     */
    @Exported
    public int getNewHighPrioritySize() {
        return getNewSizeOf(Severity.WARNING_HIGH);
    }

    /**
     * Returns the number of new normal priority issues in this analysis run.
     *
     * @return number of new normal priority issues
     */
    @Exported
    public int getNewNormalPrioritySize() {
        return getNewSizeOf(Severity.WARNING_NORMAL);
    }

    /**
     * Returns the number of new low priority issues in this analysis run.
     *
     * @return number of new low priority of issues
     */
    @Exported
    public int getNewLowPrioritySize() {
        return getNewSizeOf(Severity.WARNING_LOW);
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
