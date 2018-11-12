package io.jenkins.plugins.analysis.core.steps;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.errorprone.annotations.FormatMethod;

import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import io.jenkins.plugins.analysis.core.scm.Blames;

/**
 * A report of issues and the associated blame information, i.e. author and commit information of the SCM.
 *
 * @author Ullrich Hafner
 */
public class AnnotatedReport implements Serializable {
    private static final long serialVersionUID = -4797152016409014028L;

    private final String id;
    private final Report aggregatedReport = new Report();
    private final Blames aggregatedBlames = new Blames();
    private final Map<String, Integer> sizeOfOrigin = new HashMap();

    /**
     * Creates a new instance of {@link AnnotatedReport}. Blames and report will be initialized empty.
     *
     * @param id
     *         the ID of the report
     */
    public AnnotatedReport(@CheckForNull final String id) {
        // FIXME Pull ID out
        this.id = StringUtils.defaultIfEmpty(id, "analysis");
    }

    /**
     * Creates a new instance of {@link AnnotatedReport}. The blames will be initialized empty.
     *
     * @param id
     *         the ID of the report
     * @param report
     *         report with issues
     */
    public AnnotatedReport(@CheckForNull final String id, final Report report) {
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
    public AnnotatedReport(@CheckForNull final String id, final Report report, final Blames blames) {
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
    public AnnotatedReport(@CheckForNull final String id, final List<AnnotatedReport> reports) {
        this(id);

        for (AnnotatedReport report : reports) {
            add(report, report.getId());
        }
    }

    public Map<String, Integer> getSizeOfOrigin() {
        return new HashMap<>(sizeOfOrigin);
    }

    public Blames getBlames() {
        return aggregatedBlames;
    }

    public Report getReport() {
        return aggregatedReport;
    }

    public String getId() {
        return id;
    }

    public int size() {
        return aggregatedReport.size();
    }

    @FormatMethod
    public void logInfo(final String message, final Object... args) {
        aggregatedReport.logInfo(message, args);
    }

    public void addAll(final AnnotatedReport... reports) {
        for (AnnotatedReport report : reports) {
            add(report, report.getId());
        }
    }

    public void add(final AnnotatedReport other, final String id) {
        addReport(id, other.getReport(), other.getBlames());
    }

    public void add(final AnnotatedReport other) {
        add(other, getId());
    }

    private void addReport(final String id, final Report report, final Blames blames) {
        aggregatedReport.addAll(report);
        sizeOfOrigin.merge(id, report.size(), Integer::sum);
        aggregatedBlames.addAll(blames);
    }
}
