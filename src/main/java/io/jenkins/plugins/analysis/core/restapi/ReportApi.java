package io.jenkins.plugins.analysis.core.restapi;

import java.util.List;
import java.util.stream.Collectors;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import io.jenkins.plugins.forensics.blame.Blames;

/**
 * Remote API for a {@link Report}. Simple Java Bean that exposes several methods of a {@link Report} instance.
 *
 * @author Ullrich Hafner
 */
@ExportedBean
public class ReportApi {
    private final Report report;
    private final Blames blames;

    /**
     * Creates a new {@link ReportApi}.
     *
     * @param report
     *         the report to expose the properties from
     * @param blames
     *          the blames info for this report
     */
    public ReportApi(final Report report, final Blames blames) {
        this.report = report;
        this.blames = blames;
    }

    @Exported(inline = true)
    public List<IssueApi> getIssues() {
        return map();
    }

    private List<IssueApi> map() {
        return report.stream().map(this::createIssueApi).collect(Collectors.toList());
    }

    private IssueApi createIssueApi(final Issue issue) {
        if (blames.contains(issue.getFileName())) {
            return new IssueApi(issue, blames.getBlame(issue.getFileName()));
        }
        return new IssueApi(issue, null);
    }

    @Exported
    public int getSize() {
        return report.getSize();
    }
}
