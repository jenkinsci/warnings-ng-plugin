package io.jenkins.plugins.analysis.core.steps;

import com.google.errorprone.annotations.FormatMethod;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.Ensure;
import edu.hm.hafner.util.FilteredLog;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;

import io.jenkins.plugins.analysis.core.restapi.IssueApi;
import io.jenkins.plugins.analysis.core.restapi.ReportApi;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

/**
 * A report of issues and the associated blame information, i.e., author and commit information of the SCM.
 *
 * @author Ullrich Hafner
 */
public class AnnotatedReport implements Serializable {
    @Serial
    private static final long serialVersionUID = -4797152016409014028L;

    private final String id;
    private Report aggregatedReport = new Report();
    private final Blames aggregatedBlames = new Blames();
    private final RepositoryStatistics aggregatedRepositoryStatistics = new RepositoryStatistics();

    private final Map<String, Integer> sizeOfOrigin = new HashMap<>();

    /**
     * Creates a new instance of {@link AnnotatedReport}. Blames and report will be initialized empty.
     *
     * @param id
     *         the ID of the report
     */
    public AnnotatedReport(final String id) {
        Ensure.that(id).isNotBlank("The ID of the report must not be empty");

        this.id = id;
    }

    /**
     * Creates a new instance of {@link AnnotatedReport}. The SCM blames will be initialized empty.
     *
     * @param id
     *         the ID of the report
     * @param report
     *         report with issues
     */
    public AnnotatedReport(final String id, final Report report) {
        this(id, report, new Blames(), new RepositoryStatistics());
    }

    /**
     * Creates a new instance of {@link AnnotatedReport}.
     *
     * @param id
     *         ID of the report
     * @param report
     *         report with issues
     * @param blames
     *         author and commit information for affected files
     */
    public AnnotatedReport(final String id, final Report report, final Blames blames) {
        this(id, report, blames, new RepositoryStatistics());
    }

    /**
     * Creates a new instance of {@link AnnotatedReport}.
     *
     * @param id
     *         ID of the report
     * @param report
     *         report with issues
     * @param blames
     *         author and commit information for affected files
     * @param statistics
     *         repository statistics for affected files
     */
    public AnnotatedReport(final String id, final Report report, final Blames blames,
            final RepositoryStatistics statistics) {
        this(id);

        aggregatedReport = report;

        addBlames(id, blames, statistics, report.size());
    }

    /**
     * Creates a new instance of {@link AnnotatedReport} as an aggregation of the specified reports.
     *
     * @param id
     *         the ID of the report
     * @param reports
     *         the reports to aggregate
     */
    public AnnotatedReport(@CheckForNull final String id, final List<AnnotatedReport> reports) {
        this(id);

        aggregatedReport = new Report();
        addAllReports(reports);
    }

    /**
     * Creates a new instance of {@link AnnotatedReport} as an aggregation of the specified reports.
     *
     * @param id
     *         the ID of the report
     * @param reports
     *         the reports to aggregate
     */
    public AnnotatedReport(@CheckForNull final String id, final Iterable<AnnotatedReport> reports) {
        this(id);

        aggregatedReport = new Report();
        addAllReports(reports);
    }

    private void addAllReports(final Iterable<AnnotatedReport> reports) {
        for (AnnotatedReport report : reports) {
            addReport(report.getId(), report);
        }
    }

    private void addReport(final String reportId, final AnnotatedReport report) {
        addReport(reportId, report.getReport(), report.getBlames(), report.getStatistics());
    }

    /**
     * Returns the number of issues per origin.
     *
     * @return number of issues per origin
     */
    public Map<String, Integer> getSizeOfOrigin() {
        return new HashMap<>(sizeOfOrigin);
    }

    /**
     * Returns a read only view for the issues of this report.
     *
     * @return the issues
     */
    @Whitelisted
    public List<IssueApi> getIssues() {
        return new ReportApi(getReport(), getBlames(), List.of()).getIssues();
    }

    /**
     * Returns the aggregated report.
     *
     * @return the aggregated report
     */
    public Report getReport() {
        return aggregatedReport;
    }

    /**
     * Returns the aggregated blames for all reports.
     *
     * @return the aggregated blames
     */
    public Blames getBlames() {
        return aggregatedBlames;
    }

    /**
     * Returns the aggregated statistics for all reports.
     *
     * @return the aggregated statistics
     */
    public RepositoryStatistics getStatistics() {
        return aggregatedRepositoryStatistics;
    }

    /**
     * Returns the ID of this report.
     *
     * @return the ID
     */
    @Whitelisted
    public String getId() {
        return id;
    }

    /**
     * Returns the total number of issues of the aggregated reports.
     *
     * @return total number of issues
     */
    @Whitelisted
    public int size() {
        return aggregatedReport.size();
    }

    /**
     * Logs the specified information message. Use this method to log any useful information when composing this
     * report.
     *
     * @param format
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     * @param args
     *         Arguments referenced by the format specifiers in the format string.  If there are more arguments than
     *         format specifiers, the extra arguments are ignored.  The number of arguments is variable and may be
     *         zero.
     */
    @FormatMethod
    public void logInfo(final String format, final Object... args) {
        aggregatedReport.logInfo(format, args);
    }

    /**
     * Appends the specified {@link AnnotatedReport reports} to this report. This report will then contain the issues of
     * all specified reports, in the same order. The reports will be added with the ID of the added report.
     *
     * @param reports
     *         the reports to append
     */
    public void addAll(final Collection<AnnotatedReport> reports) {
        addAllReports(reports);
    }

    /**
     * Appends the specified {@link AnnotatedReport report} to this report. This report will then contain the issues of
     * the specified reports, appended to the end and in the same order. The report will be added with the specified
     * ID.
     *
     * @param other
     *         the other report to append
     * @param actualId
     *         the ID to use when adding the report
     */
    public void add(final AnnotatedReport other, final String actualId) {
        addReport(actualId, other);
    }

    /**
     * Appends the specified {@link AnnotatedReport report} to this report. This report will then contain the issues of
     * the specified reports, appended to the end and in the same order. The report will be added with the default ID of
     * this report.
     *
     * @param other
     *         the other report to append
     */
    public void add(final AnnotatedReport other) {
        add(other, getId());
    }

    private void addReport(final String actualId, final Report report, final Blames blames,
            final RepositoryStatistics statistics) {
        aggregatedReport.addAll(report);
        addBlames(actualId, blames, statistics, report.size());
    }

    private void addBlames(final String actualId, final Blames blames,
            final RepositoryStatistics statistics, final int size) {
        sizeOfOrigin.merge(actualId, size, Integer::sum);
        aggregatedBlames.addAll(blames);
        aggregatedRepositoryStatistics.addAll(statistics);
    }

    /**
     * Adds the specified repository statistics to this report.
     *
     * @param statistics
     *         the additional statistic
     */
    public void addRepositoryStatistics(final RepositoryStatistics statistics) {
        aggregatedRepositoryStatistics.addAll(statistics);
    }

    /**
     * Returns a logger that contains all info and error messages of the aggregated report.
     * Note that this logger will not automatically update itself when new messages that are
     * added to the report afterward.
     *
     * @return the logger with all messages
     */
    public FilteredLog getLogger() {
        var log = new FilteredLog();
        aggregatedReport.getInfoMessages().forEach(log::logInfo);
        aggregatedReport.getErrorMessages().forEach(log::logError);
        return log;
    }
}
