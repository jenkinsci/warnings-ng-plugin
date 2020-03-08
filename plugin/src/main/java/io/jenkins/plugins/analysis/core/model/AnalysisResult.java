package io.jenkins.plugins.analysis.core.model; // NOPMD

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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.charts.JenkinsBuild;
import io.jenkins.plugins.analysis.core.util.IssuesStatistics;
import io.jenkins.plugins.analysis.core.util.IssuesStatisticsBuilder;
import io.jenkins.plugins.analysis.core.util.QualityGateEvaluator;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.BlamesXmlStream;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryStatisticsXmlStream;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Stores the results of a static analysis run. Provides support for persisting the results of the build and loading and
 * saving of issues (all, new, and fixed) and delta computation.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings(value = "SE, DESERIALIZATION_GADGET", justification = "transient fields are restored using a Jenkins callback (or are checked for null)")
@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveClassLength", "PMD.GodClass", "checkstyle:ClassFanOutComplexity"})
public class AnalysisResult implements Serializable, StaticAnalysisRun {
    private static final long serialVersionUID = 1110545450292087475L;

    private static final Pattern ISSUES_FILE_NAME = Pattern.compile("issues.xml", Pattern.LITERAL);
    private static final int NO_BUILD = -1;
    private static final String NO_REFERENCE = StringUtils.EMPTY;

    private final String id;

    private IssuesStatistics totals;

    private final Map<String, Integer> sizePerOrigin;
    private final List<String> errors;
    private final List<String> messages;
    /**
     * Reference run to compute the issues difference: since a run cannot be persisted directly, the IDs are only
     * stored.
     */
    private final String referenceBuildId;

    private transient ReentrantLock lock = new ReentrantLock();
    private transient Run<?, ?> owner;

    /**
     * All outstanding issues: i.e. all issues, that are part of the current and reference report.
     */
    @Nullable
    private transient WeakReference<Report> outstandingIssuesReference;
    /**
     * All new issues: i.e. all issues, that are part of the current report but have not been shown up in the reference
     * report.
     */
    @Nullable
    private transient WeakReference<Report> newIssuesReference;
    /**
     * All fixed issues: i.e. all issues, that are part of the reference report but are not present in the current
     * report anymore.
     */
    @Nullable
    private transient WeakReference<Report> fixedIssuesReference;

    /** All SCM blames. Provides a mapping of file names to SCM commit information like author, email or commit ID. */
    @Nullable
    private transient WeakReference<Blames> blamesReference;

    /** Statistics for all files. Provides a mapping of file names to SCM statistics like #authors, #commits, etc. */
    @Nullable
    private transient WeakReference<RepositoryStatistics> repositoryStatistics;

    /** Determines since which build we have zero warnings. */
    private int noIssuesSinceBuild;
    /** Determines since which build the result is successful. */
    private int successfulSinceBuild;
    /** The result of the quality gate evaluation. */
    private final QualityGateStatus qualityGateStatus;

    /**
     * Creates a new instance of {@link AnalysisResult}.
     *
     * @param owner
     *         the current build as owner of this action
     * @param id
     *         ID of the results
     * @param report
     *         the issues of this result
     * @param blames
     *         author and commit information for all issues
     * @param totals
     *         repository statistics for all issues
     * @param qualityGateStatus
     *         the quality gate status
     * @param sizePerOrigin
     *         the number of issues per origin
     * @param previousResult
     *         the analysis result of the previous run
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public AnalysisResult(final Run<?, ?> owner, final String id, final DeltaReport report, final Blames blames,
            final RepositoryStatistics totals, final QualityGateStatus qualityGateStatus,
            final Map<String, Integer> sizePerOrigin,
            final AnalysisResult previousResult) {
        this(owner, id, report, blames, totals, qualityGateStatus, sizePerOrigin, true);

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

        if (this.qualityGateStatus == QualityGateStatus.PASSED) {
            if (previousResult.qualityGateStatus == QualityGateStatus.PASSED) {
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
     * @param id
     *         ID of the results
     * @param report
     *         the issues of this result
     * @param blames
     *         author and commit information for all issues
     * @param totals
     *         repository statistics for all issues
     * @param qualityGateStatus
     *         the quality gate status
     * @param sizePerOrigin
     *         the number of issues per origin
     */
    public AnalysisResult(final Run<?, ?> owner, final String id, final DeltaReport report, final Blames blames,
            final RepositoryStatistics totals, final QualityGateStatus qualityGateStatus,
            final Map<String, Integer> sizePerOrigin) {
        this(owner, id, report, blames, totals, qualityGateStatus, sizePerOrigin, true);

        if (report.isEmpty()) {
            noIssuesSinceBuild = owner.getNumber();
        }
        else {
            noIssuesSinceBuild = NO_BUILD;
        }
        if (this.qualityGateStatus == QualityGateStatus.PASSED) {
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
     * @param id
     *         ID of the results
     * @param report
     *         the issues of this result
     * @param blames
     *         author and commit information for all issues
     * @param repositoryStatistics
     *         source code repository statistics for all issues
     * @param qualityGateStatus
     *         the quality gate status
     * @param sizePerOrigin
     *         the number of issues per origin
     * @param canSerialize
     *         determines whether the result should be persisted in the build folder
     */
    @VisibleForTesting
    @SuppressWarnings("checkstyle:ParameterNumber")
    protected AnalysisResult(final Run<?, ?> owner, final String id, final DeltaReport report,
            final Blames blames, final RepositoryStatistics repositoryStatistics,
            final QualityGateStatus qualityGateStatus, final Map<String, Integer> sizePerOrigin,
            final boolean canSerialize) {
        this.owner = owner;

        Report allIssues = report.getAllIssues();
        this.id = id;

        totals = report.getStatistics();
        this.sizePerOrigin = new HashMap<>(sizePerOrigin);
        referenceBuildId = report.getReferenceBuildId();

        Report outstandingIssues = report.getOutstandingIssues();
        outstandingIssuesReference = new WeakReference<>(outstandingIssues);

        Report newIssues = report.getNewIssues();
        newIssuesReference = new WeakReference<>(newIssues);

        Report fixedIssues = report.getFixedIssues();
        fixedIssuesReference = new WeakReference<>(fixedIssues);

        List<String> aggregatedMessages = new ArrayList<>(allIssues.getInfoMessages().castToList());

        messages = new ArrayList<>(aggregatedMessages);
        errors = new ArrayList<>(allIssues.getErrorMessages().castToList());

        this.qualityGateStatus = qualityGateStatus;

        blamesReference = new WeakReference<>(blames);
        this.repositoryStatistics = new WeakReference<>(repositoryStatistics);

        if (canSerialize) {
            serializeIssues(outstandingIssues, newIssues, fixedIssues);
            serializeBlames(blames);
            serializeStatistics(repositoryStatistics);
        }
    }

    /**
     * Called after de-serialization to retain backward compatibility.
     *
     * @return this
     */
    protected Object readResolve() {
        if (totals == null) {
            IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

            builder.setTotalErrorSize(sizePerSeverity.getOrDefault(Severity.ERROR, 0));
            builder.setTotalHighSize(sizePerSeverity.getOrDefault(Severity.WARNING_HIGH, 0));
            builder.setTotalNormalSize(sizePerSeverity.getOrDefault(Severity.WARNING_NORMAL, 0));
            builder.setTotalLowSize(sizePerSeverity.getOrDefault(Severity.WARNING_LOW, 0));

            builder.setNewErrorSize(newSizePerSeverity.getOrDefault(Severity.ERROR, 0));
            builder.setNewHighSize(newSizePerSeverity.getOrDefault(Severity.WARNING_HIGH, 0));
            builder.setNewNormalSize(newSizePerSeverity.getOrDefault(Severity.WARNING_NORMAL, 0));
            builder.setNewLowSize(newSizePerSeverity.getOrDefault(Severity.WARNING_LOW, 0));

            builder.setFixedSize(fixedSize);
            totals = builder.build();
        }
        return this;
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
            Blames result = blamesReference.get();
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
            RepositoryStatistics result = repositoryStatistics.get();
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
        Blames blames = new BlamesXmlStream().read(getBlamesPath());
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
        RepositoryStatistics statistics = new RepositoryStatisticsXmlStream().read(getStatisticsPath());
        repositoryStatistics = new WeakReference<>(statistics);
        return statistics;
    }

    @Override
    public String getId() {
        return id;
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
    public ImmutableList<String> getErrorMessages() {
        return Lists.immutable.withAll(errors);
    }

    @Override
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
        Report report = new ReportXmlStream().read(getReportPath(suffix));
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
     */
    public boolean isSuccessful() {
        return qualityGateStatus.isSuccessful();
    }

    @Override
    public QualityGateStatus getQualityGateStatus() {
        return qualityGateStatus;
    }

    @Override
    public String toString() {
        return getId() + " : " + getTotalSize() + " issues";
    }

    @Override
    public Optional<Run<?, ?>> getReferenceBuild() {
        if (NO_REFERENCE.equals(referenceBuildId)) {
            return Optional.empty();
        }
        return new JenkinsFacade().getBuild(referenceBuildId);
    }

    @Whitelisted
    public IssuesStatistics getTotals() {
        return totals;
    }

    @Override
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
    public int getTotalSize() {
        return totals.getTotalSize();
    }

    @Override
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
     * Returns the total number of high severity issues in this analysis run.
     *
     * @return total number of high severity issues
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
     * Returns the total number of low severity issues in this analysis run.
     *
     * @return total number of low severity of issues
     */
    public int getTotalLowPrioritySize() {
        return getTotalSizeOf(Severity.WARNING_LOW);
    }

    @Override
    public int getNewSize() {
        return totals.getNewSize();
    }

    @Override
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
     * Returns the number of new high severity issues in this analysis run.
     *
     * @return number of new high severity issues
     */
    public int getNewHighPrioritySize() {
        return getNewSizeOf(Severity.WARNING_HIGH);
    }

    /**
     * Returns the number of new normal severity issues in this analysis run.
     *
     * @return number of new normal severity issues
     */
    public int getNewNormalPrioritySize() {
        return getNewSizeOf(Severity.WARNING_NORMAL);
    }

    /**
     * Returns the number of new low severity issues in this analysis run.
     *
     * @return number of new low severity of issues
     */
    public int getNewLowPrioritySize() {
        return getNewSizeOf(Severity.WARNING_LOW);
    }

    @Override
    public int getFixedSize() {
        return totals.getFixedSize();
    }

    public int getDeltaSize() {
        return totals.getDeltaSize();
    }

    /**
     * Old serialization item.
     *
     * @deprecated Replaced by {@link AnalysisResult#totals}.
     */
    @Deprecated
    private transient int size;
    /**
     * Old serialization item.
     *
     * @deprecated Replaced by {@link AnalysisResult#totals}.
     */
    @Deprecated
    private transient int newSize;
    /**
     * Old serialization item.
     *
     * @deprecated Replaced by {@link AnalysisResult#totals}.
     */
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    private transient int fixedSize;
    /**
     * Old serialization item.
     *
     * @deprecated Replaced by {@link AnalysisResult#totals}.
     */
    @Deprecated
    @SuppressWarnings({"DeprecatedIsStillUsed", "MismatchedQueryAndUpdateOfCollection"})
    private final Map<Severity, Integer> sizePerSeverity = new HashMap<>();
    /**
     * Old serialization item.
     *
     * @deprecated Replaced by {@link AnalysisResult#totals}.
     */
    @Deprecated
    @SuppressWarnings({"DeprecatedIsStillUsed", "MismatchedQueryAndUpdateOfCollection"})
    private final Map<Severity, Integer> newSizePerSeverity = new HashMap<>();

    public Build getBuild() {
        return new JenkinsBuild(getOwner());
    }
}
