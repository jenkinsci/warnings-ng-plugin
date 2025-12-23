package io.jenkins.plugins.analysis.core.model; // NOPMD

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serial;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.charts.JenkinsBuild;
import io.jenkins.plugins.analysis.core.util.IssuesStatistics;
import io.jenkins.plugins.analysis.core.util.IssuesStatisticsBuilder;
import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.BlamesXmlStream;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryStatisticsXmlStream;
import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.QualityGateEvaluator;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.QualityGateStatus;
import io.jenkins.plugins.util.ValidationUtilities;

/**
 * Stores the results of a static analysis run. Provides support for persisting the results of the build and loading and
 * saving of issues (all, new, and fixed) and delta computation.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings(value = "SE, DESERIALIZATION_GADGET", justification = "transient fields are restored using a Jenkins callback (or are checked for null)")
@SuppressWarnings({"PMD.GodClass", "PMD.CyclomaticComplexity", "checkstyle:ClassFanOutComplexity", "checkstyle:ClassDataAbstractionCoupling"})
public class AnalysisResult implements Serializable, StaticAnalysisRun {
    @Serial
    private static final long serialVersionUID = 1110545450292087475L;

    private static final Pattern ISSUES_FILE_NAME = Pattern.compile("issues.xml", Pattern.LITERAL);
    private static final int NO_BUILD = -1;
    private static final String NO_REFERENCE = StringUtils.EMPTY;

    private final String id;
    private /* almost final */ String parserId;

    private IssuesStatistics totals;

    private final Map<String, Integer> sizePerOrigin;
    private final List<String> errors;
    private final List<String> messages;
    private final List<String> sourceDirectories; // @since 13.x.0
    /**
     * Reference run to compute the issues difference: since a run cannot be persisted directly, the IDs are only
     * stored.
     */
    private final String referenceBuildId;

    private transient ReentrantLock lock = new ReentrantLock();
    private transient Run<?, ?> owner;

    /**
     * All outstanding issues: i.e., all issues, that are part of the current and reference report.
     */
    @CheckForNull
    private transient WeakReference<Report> outstandingIssuesReference;
    /**
     * All new issues: i.e., all issues, that are part of the current report but have not been shown up in the reference
     * report.
     */
    @CheckForNull
    private transient WeakReference<Report> newIssuesReference;
    /**
     * All fixed issues: i.e., all issues, that are part of the reference report but are not present in the current
     * report anymore.
     */
    @CheckForNull
    private transient WeakReference<Report> fixedIssuesReference;

    /** All SCM blames. Provides a mapping of file names to SCM commit information like author, email or commit ID. */
    @CheckForNull
    private transient WeakReference<Blames> blamesReference;

    /** Statistics for all files. Provides a mapping of file names to SCM statistics like #authors, #commits, etc. */
    @CheckForNull
    private transient WeakReference<RepositoryStatistics> repositoryStatistics;

    /** Determines since which build we have zero warnings. */
    private int noIssuesSinceBuild;
    /** Determines since which build the result is successful. */
    private int successfulSinceBuild;
    /** The result of the quality gate evaluation. */
    private transient QualityGateStatus qualityGateStatus;
    /** The result of the quality gate evaluation. */
    private QualityGateResult qualityGateResult;

    static {
        Run.XSTREAM2.alias("item", QualityGateResult.QualityGateResultItem.class);
    }

    /**
     * Creates a new instance of {@link AnalysisResult}.
     *
     * @param owner
     *         the current build as the owner of this action
     * @param id
     *         ID of the results
     * @param report
     *         the issues of this result
     * @param blames
     *         author and commit information for all issues
     * @param totals
     *         repository statistics for all issues
     * @param qualityGateResult
     *         the quality gate status
     * @param sizePerOrigin
     *         the number of issues per origin
     * @param sourceDirectories
     *         list of configured source directories
     * @param previousResult
     *         the analysis result of the previous run
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public AnalysisResult(final Run<?, ?> owner, final String id, final DeltaReport report, final Blames blames,
            final RepositoryStatistics totals, final QualityGateResult qualityGateResult,
            final Map<String, Integer> sizePerOrigin, final List<String> sourceDirectories, final AnalysisResult previousResult) {
        this(owner, id, report, blames, totals, qualityGateResult, sizePerOrigin, sourceDirectories, true);

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

        var overallStatus = qualityGateResult.getOverallStatus();
        if (overallStatus == QualityGateStatus.PASSED) {
            if (previousResult.getQualityGateResult().getOverallStatus() == QualityGateStatus.PASSED) {
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
     *         the current build as the owner of this action
     * @param id
     *         ID of the results
     * @param report
     *         the issues of this result
     * @param blames
     *         author and commit information for all issues
     * @param totals
     *         repository statistics for all issues
     * @param qualityGateResult
     *         the quality gate status
     * @param sizePerOrigin
     *         the number of issues per origin
     * @param sourceDirectories
     *         list of configured source directories
     */
    public AnalysisResult(final Run<?, ?> owner, final String id, final DeltaReport report, final Blames blames,
            final RepositoryStatistics totals, final QualityGateResult qualityGateResult,
            final Map<String, Integer> sizePerOrigin, final List<String> sourceDirectories) {
        this(owner, id, report, blames, totals, qualityGateResult, sizePerOrigin, sourceDirectories, true);

        if (report.isEmpty()) {
            noIssuesSinceBuild = owner.getNumber();
        }
        else {
            noIssuesSinceBuild = NO_BUILD;
        }
        if (qualityGateResult.getOverallStatus() == QualityGateStatus.PASSED) {
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
     *         the current run as the owner of this action
     * @param id
     *         ID of the results
     * @param report
     *         the issues of this result
     * @param blames
     *         author and commit information for all issues
     * @param repositoryStatistics
     *         source code repository statistics for all issues
     * @param qualityGateResult
     *         the quality gate status
     * @param sizePerOrigin
     *         the number of issues per origin
     * @param sourceDirectories
     *         list of configured source directories
     * @param canSerialize
     *         determines whether the result should be persisted in the build folder
     */
    @VisibleForTesting
    @SuppressWarnings({"checkstyle:ParameterNumber", "PMD.ConstructorCallsOverridableMethod"})
    protected AnalysisResult(final Run<?, ?> owner, final String id, final DeltaReport report,
            final Blames blames, final RepositoryStatistics repositoryStatistics,
            final QualityGateResult qualityGateResult, final Map<String, Integer> sizePerOrigin,
            final List<String> sourceDirectories, final boolean canSerialize) {
        this.owner = owner;

        var allIssues = report.getAllIssues();

        new ValidationUtilities().ensureValidId(id);
        this.id = id;
        this.parserId = allIssues.getParserId();

        totals = report.getStatistics();
        this.sizePerOrigin = new HashMap<>(sizePerOrigin);
        this.sourceDirectories = new ArrayList<>(sourceDirectories);
        referenceBuildId = report.getReferenceBuildId();

        var outstandingIssues = report.getOutstandingIssues();
        outstandingIssuesReference = new WeakReference<>(outstandingIssues);

        var newIssues = report.getNewIssues();
        newIssuesReference = new WeakReference<>(newIssues);

        var fixedIssues = report.getFixedIssues();
        fixedIssuesReference = new WeakReference<>(fixedIssues);

        List<String> aggregatedMessages = new ArrayList<>(allIssues.getInfoMessages());

        messages = new ArrayList<>(aggregatedMessages);
        errors = new ArrayList<>(allIssues.getErrorMessages());

        this.qualityGateResult = qualityGateResult;

        blamesReference = new WeakReference<>(blames);
        this.repositoryStatistics = new WeakReference<>(repositoryStatistics);

        if (canSerialize) {
            serializeIssues(outstandingIssues, newIssues, fixedIssues);
            serializeBlames(blames);
            serializeStatistics(repositoryStatistics);
        }
    }

    /**
     * Called after deserialization to retain backward compatibility.
     *
     * @return this
     */
    @Serial
    protected Object readResolve() {
        if (qualityGateResult == null && qualityGateStatus != null) {
            qualityGateResult = new QualityGateResult(qualityGateStatus);
        }
        if (totals == null) {
            totals = new IssuesStatisticsBuilder().build();
        }
        if (parserId == null) {
            parserId = id; // fallback for old data
        }
        return this;
    }

    /**
     * Returns the list of configured source directories.
     *
     * @return the source directories
     */
    public List<String> getSourceDirectories() {
        if (sourceDirectories == null) {
            return new ArrayList<>(); 
        }
        return new ArrayList<>(sourceDirectories);
    }

    /**
     * Returns the blames for the report.
     *
     * @return the blames
     */
    public Blames getBlames() {
        lock.lock();
        try {
            if (blamesReference == null) {
                return readBlames();
            }
            var result = blamesReference.get();
            if (result == null) {
                return readBlames();
            }
            return result;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Returns the repository statistics for the report.
     *
     * @return the statistics
     */
    public RepositoryStatistics getForensics() {
        lock.lock();
        try {
            if (repositoryStatistics == null) {
                return readStatistics();
            }
            var result = repositoryStatistics.get();
            if (result == null) {
                return readStatistics();
            }
            return result;
        }
        finally {
            lock.unlock();
        }
    }

    private void serializeBlames(final Blames blames) {
        new BlamesXmlStream().write(getBlamesPath(), blames);
    }

    private Path getBlamesPath() {
        return getOwner().getRootDir().toPath().resolve(id + "-blames.xml");
    }

    private Blames readBlames() {
        var blames = new BlamesXmlStream().read(getBlamesPath());
        blamesReference = new WeakReference<>(blames);
        return blames;
    }

    private void serializeStatistics(final RepositoryStatistics statistics) {
        new RepositoryStatisticsXmlStream().write(getStatisticsPath(), statistics);
    }

    private Path getStatisticsPath() {
        return getOwner().getRootDir().toPath().resolve(id + "-forensics.xml");
    }

    private RepositoryStatistics readStatistics() {
        var statistics = new RepositoryStatisticsXmlStream().read(getStatisticsPath());
        repositoryStatistics = new WeakReference<>(statistics);
        return statistics;
    }

    @Whitelisted
    @Override
    public String getId() {
        return id;
    }

    String getParserId() {
        return parserId;
    }

    @Override
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

    @Override
    @Whitelisted
    public ImmutableList<String> getErrorMessages() {
        return Lists.immutable.withAll(errors);
    }

    @Override
    @Whitelisted
    public ImmutableList<String> getInfoMessages() {
        return Lists.immutable.withAll(messages);
    }

    private String getSerializationFileName() {
        return id + "-issues.xml";
    }

    private void serializeIssues(final Report outstandingIssues, final Report newIssues, final Report fixedIssues) {
        serializeIssues(outstandingIssues, "outstanding");
        serializeIssues(newIssues, "new");
        serializeIssues(fixedIssues, "fixed");
    }

    private void serializeIssues(final Report report, final String suffix) {
        new ReportXmlStream().write(getReportPath(suffix), report);
    }

    private Path getReportPath(final String suffix) {
        return getOwner().getRootDir().toPath().resolve(ISSUES_FILE_NAME.matcher(getSerializationFileName())
                .replaceAll(Matcher.quoteReplacement(suffix + "-issues.xml")));
    }

    /**
     * Returns all issues of the associated static analysis run. These include outstanding issues as well as new
     * issues.
     *
     * @return all issues
     */
    @Whitelisted
    public Report getIssues() {
        var merged = new Report();
        merged.addAll(getNewIssues(), getOutstandingIssues());
        return merged;
    }

    /**
     * Check if {@link AnalysisResult} issues are empty (including new, outstanding and fixed).
     *
     * @return
     *          true if {@link AnalysisResult} issues are empty, else false.
     */
    public boolean isEmpty() {
        return getTotals().getTotalSize() + getTotals().getFixedSize() == 0;
    }

    /**
     * Check if {@link AnalysisResult} issues does not have any new warnings.
     *
     * @return
     *          true if {@link AnalysisResult} issues has no new warnings.
     */
    public boolean hasNoNewWarnings() {
        return getTotals().getNewSize() == 0;
    }

    /**
     * Returns all outstanding issues of the associated static analysis run. I.e., all issues that are part of the
     * current and previous report.
     *
     * @return all outstanding issues
     */
    @Whitelisted
    public Report getOutstandingIssues() {
        return getIssues(AnalysisResult::getOutstandingIssuesReference, AnalysisResult::setOutstandingIssuesReference,
                "outstanding");
    }

    /**
     * Returns all new issues of the associated static analysis run. I.e., all issues that are part of the current
     * report but have not been shown up in the previous report.
     *
     * @return all new issues
     */
    @Whitelisted
    public Report getNewIssues() {
        return getIssues(AnalysisResult::getNewIssuesReference, AnalysisResult::setNewIssuesReference,
                "new");
    }

    /**
     * Returns all fixed issues of the associated static analysis run. I.e., all issues that are part of the previous
     * report but are not present in the current report anymore.
     *
     * @return all fixed issues
     */
    @Whitelisted
    public Report getFixedIssues() {
        return getIssues(AnalysisResult::getFixedIssuesReference, AnalysisResult::setFixedIssuesReference,
                "fixed");
    }

    @CheckForNull
    private WeakReference<Report> getOutstandingIssuesReference() {
        return outstandingIssuesReference;
    }

    private void setOutstandingIssuesReference(final WeakReference<Report> outstandingIssuesReference) {
        this.outstandingIssuesReference = outstandingIssuesReference;
    }

    @CheckForNull
    private WeakReference<Report> getNewIssuesReference() {
        return newIssuesReference;
    }

    private void setNewIssuesReference(final WeakReference<Report> newIssuesReference) {
        this.newIssuesReference = newIssuesReference;
    }

    @CheckForNull
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
            var result = getter.apply(this).get();
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
        var report = new ReportXmlStream().read(getReportPath(suffix));
        setter.accept(this, new WeakReference<>(report));
        return report;
    }

    @Override
    public int getNoIssuesSinceBuild() {
        return noIssuesSinceBuild;
    }

    @Override
    public int getSuccessfulSinceBuild() {
        return successfulSinceBuild;
    }

    /**
     * Returns whether the static analysis result is successful with respect to the defined {@link
     * QualityGateEvaluator}.
     *
     * @return {@code true} if the static analysis result is successful, {@code false} if the static analysis result is
     *         {@link QualityGateStatus#WARNING} or {@link QualityGateStatus#FAILED}
     * @see QualityGateEvaluator
     */
    public boolean isSuccessful() {
        return qualityGateResult.isSuccessful();
    }

    @Whitelisted
    @Override
    public QualityGateStatus getQualityGateStatus() {
        return qualityGateResult.getOverallStatus();
    }

    @Whitelisted
    @Override
    public QualityGateResult getQualityGateResult() {
        return qualityGateResult;
    }

    @Override
    public String toString() {
        return getId() + " : " + getTotalSize() + " issues";
    }

    @Override
    @Whitelisted
    public Optional<Run<?, ?>> getReferenceBuild() {
        if (NO_REFERENCE.equals(referenceBuildId)) {
            return Optional.empty();
        }
        return new JenkinsFacade().getBuild(referenceBuildId);
    }

    @Override
    @Whitelisted
    public IssuesStatistics getTotals() {
        return totals;
    }

    @Override
    @Whitelisted
    public Map<String, Integer> getSizePerOrigin() {
        return Maps.immutable.ofAll(sizePerOrigin).toMap();
    }

    /**
     * Returns the number of issues in this analysis run, mapped by {@link Severity}.
     *
     * @return number of issues per severity
     */
    public Map<Severity, Integer> getSizePerSeverity() {
        return totals.getTotalSizePerSeverity().toMap();
    }

    /**
     * Returns the new number of issues in this analysis run, mapped by {@link Severity}.
     *
     * @return number of issues per severity
     */
    public Map<Severity, Integer> getNewSizePerSeverity() {
        return totals.getTotalSizePerSeverity().toMap();
    }

    @Override
    @Whitelisted
    public int getTotalSize() {
        return totals.getTotalSize();
    }

    @Override
    @Whitelisted
    public int getTotalSizeOf(final Severity severity) {
        return totals.getTotalSizeOf(severity);
    }

    /**
     * Returns the total number of errors in this analysis run.
     *
     * @return total number of errors
     */
    public int getTotalErrorsSize() {
        return getTotalSizeOf(Severity.ERROR);
    }

    /**
     * Returns the total number of high-severity issues in this analysis run.
     *
     * @return total number of high-severity issues
     */
    public int getTotalHighPrioritySize() {
        return getTotalSizeOf(Severity.WARNING_HIGH);
    }

    /**
     * Returns the total number of normal severity issues in this analysis run.
     *
     * @return total number of normal severity issues
     */
    public int getTotalNormalPrioritySize() {
        return getTotalSizeOf(Severity.WARNING_NORMAL);
    }

    /**
     * Returns the total number of low-severity issues in this analysis run.
     *
     * @return total number of low-severity issues
     */
    public int getTotalLowPrioritySize() {
        return getTotalSizeOf(Severity.WARNING_LOW);
    }

    @Override
    @Whitelisted
    public int getNewSize() {
        return totals.getNewSize();
    }

    @Override
    @Whitelisted
    public int getNewSizeOf(final Severity severity) {
        return totals.getNewSizeOf(severity);
    }

    /**
     * Returns the number of new errors in this analysis run.
     *
     * @return number of new errors issues
     */
    public int getNewErrorSize() {
        return getNewSizeOf(Severity.ERROR);
    }

    /**
     * Returns the number of new high-severity issues in this analysis run.
     *
     * @return number of new high-severity issues
     */
    public int getNewHighPrioritySize() {
        return getNewSizeOf(Severity.WARNING_HIGH);
    }

    /**
     * Returns the number of new normal-severity issues in this analysis run.
     *
     * @return number of new normal-severity issues
     */
    public int getNewNormalPrioritySize() {
        return getNewSizeOf(Severity.WARNING_NORMAL);
    }

    /**
     * Returns the number of new low-severity issues in this analysis run.
     *
     * @return number of new low-severity issues
     */
    public int getNewLowPrioritySize() {
        return getNewSizeOf(Severity.WARNING_LOW);
    }

    @Override
    @Whitelisted
    public int getFixedSize() {
        return totals.getFixedSize();
    }

    public int getDeltaSize() {
        return totals.getDeltaSize();
    }

    public Build getBuild() {
        return new JenkinsBuild(getOwner());
    }
}
