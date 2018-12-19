package io.jenkins.plugins.analysis.core.restapi;

import java.util.List;
import java.util.stream.Collectors;

import edu.hm.hafner.analysis.Report;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Remote API for a {@link Report}. Simple Java Bean that exposes several methods of a {@link Report} instance.
 *
 * @author Ullrich Hafner
 */
@ExportedBean
public class ReportApi {
    private final Report report;

    /**
     * Creates a new {@link ReportApi}.
     *
     * @param report
     *         the report to expose the properties from
     */
    public ReportApi(final Report report) {
        this.report = report;
    }

    @Exported(inline = true)
    public List<IssueApi> getIssues() {
        return map();
    }

    private List<IssueApi> map() {
        return report.stream().map(IssueApi::new).collect(Collectors.toList());
    }

    @Exported
    public int getSize() {
        return report.getSize();
    }
}
