package io.jenkins.plugins.analysis.core.steps;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.util.IssuesStatistics;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.checks.api.ChecksAnnotation;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationBuilder;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationLevel;
import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksOutput.ChecksOutputBuilder;
import io.jenkins.plugins.checks.api.ChecksPublisher;
import io.jenkins.plugins.checks.api.ChecksPublisherFactory;
import io.jenkins.plugins.checks.api.ChecksStatus;

/**
 * Publishes warnings as checks to scm platforms.
 *
 * @author Kezhi Xiong
 */
class WarningChecksPublisher {
    private final ResultAction action;

    WarningChecksPublisher(final ResultAction action) {
        this.action = action;
    }

    /**
     * Publishes checks to platforms. Afterwards, all warnings are available in corresponding platform's UI,
     * e.g. GitHub checks.
     */
    void publishChecks() {
        ChecksPublisher publisher = ChecksPublisherFactory.fromRun(action.getOwner());
        publisher.publish(extractChecksDetails());
    }

    @VisibleForTesting
    ChecksDetails extractChecksDetails() {
        AnalysisResult result = action.getResult();
        IssuesStatistics totals = result.getTotals();

        StaticAnalysisLabelProvider labelProvider = action.getLabelProvider();

        return new ChecksDetailsBuilder()
                .withName(labelProvider.getName())
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(extractChecksConclusion(result.getQualityGateStatus()))
                .withOutput(new ChecksOutputBuilder()
                        .withTitle(labelProvider.getLinkName())
                        .withSummary(extractChecksSummary(totals))
                        .withText(extractChecksText(totals))
                        .withAnnotations(extractChecksAnnotations(result.getNewIssues()))
                        .build())
                .withDetailsURL(action.getAbsoluteUrl())
                .build();
    }

    private String extractChecksSummary(final IssuesStatistics statistics) {
        return String.format("## %d issues in total:\n"
                        + "- ### %d new issues\n"
                        + "- ### %d outstanding issues\n"
                        + "- ### %d delta issues\n"
                        + "- ### %d fixed issues",
                statistics.getTotalSize(), statistics.getNewSize(), statistics.getTotalSize() - statistics.getNewSize(),
                statistics.getDeltaSize(), statistics.getFixedSize());
    }

    private String extractChecksText(final IssuesStatistics statistics) {
        return "## Total Issue Statistics:\n"
                + generateSeverityText(statistics.getTotalLowSize(), statistics.getTotalNormalSize(),
                statistics.getTotalHighSize(), statistics.getTotalErrorSize())
                + "## New Issue Statistics:\n"
                + generateSeverityText(statistics.getNewLowSize(), statistics.getNewNormalSize(),
                statistics.getNewHighSize(), statistics.getNewErrorSize())
                + "## Delta Issue Statistics:\n"
                + generateSeverityText(statistics.getDeltaLowSize(), statistics.getDeltaNormalSize(),
                statistics.getDeltaHighSize(), statistics.getDeltaErrorSize());
    }

    private String generateSeverityText(final int low, final int normal, final int high, final int error) {
        return "* Error: " + error + "\n"
                + "* High: " + high + "\n"
                + "* Normal: " + normal + "\n"
                + "* Low: " + low + "\n";
    }

    private ChecksConclusion extractChecksConclusion(final QualityGateStatus status) {
        switch (status) {
            case INACTIVE:
            case PASSED:
                return ChecksConclusion.SUCCESS;
            case FAILED:
            case WARNING:
                return ChecksConclusion.FAILURE;
            default:
                throw new IllegalArgumentException("Unsupported quality gate status: " + status);
        }
    }

    // TODO: Use IconLabelProvider to extract description
    private List<ChecksAnnotation> extractChecksAnnotations(final Report issues) {
        return issues.stream()
                .map(issue -> new ChecksAnnotationBuilder()
                        .withPath(issue.getFileName())
                        .withTitle(issue.getType())
                        .withAnnotationLevel(ChecksAnnotationLevel.WARNING)
                        .withMessage(issue.getSeverity() + ":\n" + parseHtml(issue.getMessage()))
                        .withStartLine(issue.getLineStart())
                        .withEndLine(issue.getLineEnd())
                        .withStartColumn(issue.getColumnStart())
                        .withEndColumn(issue.getColumnEnd())
                        .withRawDetails(issue.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private String parseHtml(final String html) {
        Set<String> contents = new HashSet<>();
        parseHtml(Jsoup.parse(html), contents);
        return String.join("\n", contents);
    }

    private void parseHtml(final Element html, final Set<String> contents) {
        for (TextNode node : html.textNodes()) {
            contents.add(node.text().trim());
        }

        for (Element child : html.children()) {
            if (child.hasAttr("href")) {
                contents.add(child.text().trim() + ":" + child.attr("href").trim());
            } else {
                parseHtml(child, contents);
            }
        }
    }
}
