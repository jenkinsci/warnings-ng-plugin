package io.jenkins.plugins.analysis.core.steps;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.errorprone.annotations.FormatMethod;

import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.Nullable;

import io.jenkins.plugins.forensics.blame.Blames;

/**
 * A report of issues and the associated blame information, i.e. author and commit information of the SCM.
 *
 * @author Ullrich Hafner
 */
public final class AnnotatedReport implements Serializable {
    private static final long serialVersionUID = -4797152016409014028L;

    private final String id;
    private final Report aggregatedReport = new Report();
    private final Blames aggregatedBlames = new Blames();
    private final Map<String, Integer> sizeOfOrigin = new HashMap<>();

    /**
     * Creates a new instance of {@link AnnotatedReport}. Blames and report will be initialized empty.
     *
     * @param id
     *         the ID of the report
     */
    public AnnotatedReport(final String id) {
        this.id = id;
    }

    /**
     * Creates a new instance of {@link AnnotatedReport}. The blames will be initialized empty.
     *
     * @param id
     *         the ID of the report
     * @param report
     *         report with issues
     */
    public AnnotatedReport(@Nullable final String id, final Report report) {
        this(id, report, new Blames());
    }

    /**
     * Creates a new instance of {@link AnnotatedReport}.
     *
     * @param id
     *         ID of the report
     * @param report
     *         report with issues
     * @param blames
     *         author and commit information
     */
    public AnnotatedReport(@Nullable final String id, final Report report, final Blames blames) {
        this(id);

        addReport(id, report, blames);
    }

    /**
     * Creates a new instance of {@link AnnotatedReport} as an aggregation of the specified reports.
     *
     * @param id
     *         the ID of the report
     * @param reports
     *         the reports to aggregate
     */
    public AnnotatedReport(@Nullable final String id, final List<AnnotatedReport> reports) {
        this(id);

        for (AnnotatedReport report : reports) {
            add(report, report.getId());
        }
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
     * Returns the aggregated blames for all reports.
     *
     * @return the blames
     */
    public Blames getBlames() {
        return aggregatedBlames;
    }

    /**
     * Returns the aggregated report.
     *
     * @return the blames
     */
    public Report getReport() {
        return aggregatedReport;
    }

    /**
     * Returns the ID of this report.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the total number of issues of the aggregated reports.
     *
     * @return total number of issues
     */
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
        for (AnnotatedReport report : reports) {
            add(report, report.getId());
        }
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
        addReport(actualId, other.getReport(), other.getBlames());
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

    private void addReport(final String actualId, final Report report, final Blames blames) {
        aggregatedReport.addAll(report);
        sizeOfOrigin.merge(actualId, report.size(), Integer::sum);
        aggregatedBlames.addAll(blames);
    }
}
