package io.jenkins.plugins.analysis.core.steps;

import java.io.Serializable;

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

    public AnnotatedReport() {
        report = new Report();
        blames = new Blames();
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
            report.addAll(annotatedReport.getReport());
            blames.add(annotatedReport.getBlames());
        }
    }
}
