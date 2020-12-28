package io.jenkins.plugins.analysis.core.steps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;

import hudson.model.TaskListener;

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
    private final TaskListener listener;

    WarningChecksPublisher(final ResultAction action, final TaskListener listener) {
        this.action = action;
        this.listener = listener;
    }

    /**
     * Publishes checks to platforms. Afterwards, all warnings are available in corresponding platform's UI, e.g. GitHub
     * checks.
     */
    void publishChecks() {
        ChecksPublisher publisher = ChecksPublisherFactory.fromRun(action.getOwner(), listener);
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
                        .withTitle(extractChecksTitle(totals))
                        .withSummary(extractChecksSummary(totals))
                        .withText(extractChecksText(totals))
                        .withAnnotations(extractChecksAnnotations(result.getNewIssues(), labelProvider))
                        .build())
                .withDetailsURL(action.getAbsoluteUrl())
                .build();
    }

    private String extractChecksTitle(final IssuesStatistics statistics) {
        if (statistics.getTotalSize() == 0) {
            return "No issues.";
        }
        else if (statistics.getNewSize() == 0) {
            return String.format("No new issues, %d total.", statistics.getTotalSize());
        }
        else if (statistics.getNewSize() == statistics.getTotalSize()) {
            if (statistics.getNewSize() == 1) {
                return "1 new issue.";
            }
            return String.format("%d new issues.", statistics.getNewSize());
        }
        else {
            if (statistics.getNewSize() == 1) {
                return String.format("1 new issue, %d total.", statistics.getTotalSize());
            }
            return String.format("%d new issues, %d total.", statistics.getNewSize(), statistics.getTotalSize());
        }
    }

    private String formatColumns(final Object[] columns) {
        StringBuilder row = new StringBuilder();
        for (Object column : columns) {
            row.append(String.format("|%s", column));
        }
        row.append('\n');
        return row.toString();
    }

    private String extractChecksSummary(final IssuesStatistics statistics) {
        StringBuilder summary = new StringBuilder();

        summary.append(formatColumns(
                new String[] {"Total", "New", "Outstanding", "Fixed", "Trend"}));
        summary.append(formatColumns(new String[] {":-:", ":-:", ":-:", ":-:", ":-:"}));
        summary.append(formatColumns(new Object[] {
                statistics.getTotalSize(),
                statistics.getNewSize(),
                statistics.getTotalSize() - statistics.getNewSize(),
                statistics.getFixedSize(),
                getTrendEmoji(statistics)}));

        return summary.toString();
    }

    private String getTrendEmoji(final IssuesStatistics statistics) {
        if (statistics.getTotalSize() == 0) {
            return ":clap:";
        }
        if (statistics.getFixedSize() > statistics.getNewSize()) {
            return ":+1:";
        }
        if (statistics.getNewSize() > 0) {
            return ":-1:";
        }
        return ":zzz:";
    }

    private String extractChecksText(final IssuesStatistics statistics) {
        if (statistics.getNewSize() == 0) {
            return "## Severity distribution of all issues\n"
                + generateSeverityText(statistics.getTotalErrorSize(), statistics.getTotalHighSize(),
                statistics.getTotalNormalSize(), statistics.getTotalLowSize());
        }
        else {
            return "## Severity distribution of new issues\n"
                    + generateSeverityText(statistics.getNewErrorSize(), statistics.getNewHighSize(),
                    statistics.getNewNormalSize(), statistics.getNewLowSize());
        }
    }

    private String generateSeverityText(final int error, final int high, final int normal, final int low) {
        StringBuilder summary = new StringBuilder();

        summary.append(formatColumns(
                new String[] {"Error", "Warning High", "Warning Normal", "Warning Low"}));
        summary.append(formatColumns(new String[] {":-:", ":-:", ":-:", ":-:"}));
        summary.append(formatColumns(new Object[] {error, high, normal, low}));

        return summary.toString();
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

    private List<ChecksAnnotation> extractChecksAnnotations(final Report issues,
            final StaticAnalysisLabelProvider labelProvider) {
        List<ChecksAnnotation> annotations = new ArrayList<>(issues.getSize());

        for (Issue issue : issues) {
            ChecksAnnotationBuilder builder = new ChecksAnnotationBuilder()
                    .withPath(issue.getFileName())
                    .withTitle(issue.getType())
                    .withAnnotationLevel(ChecksAnnotationLevel.WARNING)
                    .withMessage(issue.getSeverity() + ":\n" + parseHtml(issue.getMessage()))
                    .withStartLine(issue.getLineStart())
                    .withEndLine(issue.getLineEnd())
                    .withRawDetails(StringUtils.normalizeSpace(labelProvider.getDescription(issue)));

            if (issue.getLineStart() == issue.getLineEnd()) {
                builder.withStartColumn(issue.getColumnStart())
                        .withEndColumn(issue.getColumnEnd());
            }

            annotations.add(builder.build());
        }

        return annotations;
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
            }
            else {
                parseHtml(child, contents);
            }
        }
    }
}
