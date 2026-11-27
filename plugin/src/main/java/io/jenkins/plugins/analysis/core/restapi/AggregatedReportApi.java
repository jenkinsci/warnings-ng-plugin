package io.jenkins.plugins.analysis.core.restapi;

import java.util.List;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Remote API for the issues of several tools. Exposes the issues of all reports in a single list, the same way
 * {@link ReportApi} exposes the issues of a single report. The tool that reported an issue is available in the
 * property {@code origin} of each issue.
 *
 * @author Ullrich Hafner
 */
@ExportedBean
public class AggregatedReportApi {
    private final List<ReportApi> reports;

    /**
     * Creates a new {@link AggregatedReportApi}.
     *
     * @param reports
     *         the reports of the tools to aggregate
     */
    public AggregatedReportApi(final List<ReportApi> reports) {
        this.reports = List.copyOf(reports);
    }

    @Exported(inline = true)
    public List<IssueApi> getIssues() {
        return reports.stream().map(ReportApi::getIssues).flatMap(List::stream).toList();
    }

    @Exported
    public int getSize() {
        return reports.stream().mapToInt(ReportApi::getSize).sum();
    }
}
