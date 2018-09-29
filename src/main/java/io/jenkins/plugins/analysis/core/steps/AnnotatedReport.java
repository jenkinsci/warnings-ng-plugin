package io.jenkins.plugins.analysis.core.steps;

import java.io.Serializable;
import java.util.List;

import com.google.errorprone.annotations.FormatMethod;

import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.scm.Blames;

/**
 * A report of issues and the associated blame information, i.e. author and commit information of the SCM.
 *
 * @author Ullrich Hafner
 */
public class AnnotatedReport implements Serializable {
    private static final long serialVersionUID = -4797152016409014028L;

    private final Report report;
    private final Blames blames;

    /**
     * Creates a new instance of {@link AnnotatedReport}.
     *
     * @param report
     *         report with issues
     * @param blames
     *         author and commit information
     */
    public AnnotatedReport(final Report report, final Blames blames) {
        this.report = report;
        this.blames = blames;
    }

    /**
     * Creates a new instance of {@link AnnotatedReport}. The blames will be initialized empty.
     *
     * @param report
     *         report with issues
     */
    public AnnotatedReport(final Report report) {
        this(report, new Blames());
    }

    /**
     * Creates a new instance of {@link AnnotatedReport}. Blames and report will be initialized empty.
     */
    public AnnotatedReport() {
        this(new Report());
    }

    /**
     * Creates a new instance of {@link AnnotatedReport} as an aggregation of the specified reports. 
     */
    public AnnotatedReport(final List<AnnotatedReport> reports) {
        this();
        
        for (AnnotatedReport report : reports) {
            add(report);
        }
    }

    public Blames getBlames() {
        return blames;
    }

    public Report getReport() {
        return report;
    }

    public String getId() {
        return report.getId();
    }

    public int size() {
        return report.size();
    }

    public void setId(final String id) {
        report.setId(id);
    }

    @FormatMethod
    public void logInfo(final String message, final Object... args) {
        report.logInfo(message, args);
    }

    public void addAll(final AnnotatedReport... reports) {
        for (AnnotatedReport annotatedReport : reports) {
            add(annotatedReport);
        }
    }

    private void add(final AnnotatedReport annotatedReport) {
        report.addAll(annotatedReport.getReport());
        blames.addAll(annotatedReport.getBlames());
    }
}
