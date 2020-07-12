package io.jenkins.plugins.analysis.core.steps;

import java.util.List;
import java.util.stream.Collectors;

import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.util.IssuesStatistics;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.checks.api.ChecksAnnotation;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationBuilder;
import io.jenkins.plugins.checks.api.ChecksAnnotation.ChecksAnnotationLevel;
import io.jenkins.plugins.checks.api.ChecksConclusion;
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
        AnalysisResult result = action.getResult();
        IssuesStatistics totals = result.getTotals();

        StaticAnalysisLabelProvider labelProvider = action.getLabelProvider();

        ChecksPublisher publisher = ChecksPublisherFactory.fromRun(action.getOwner());
        publisher.publish(new ChecksDetailsBuilder()
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
                .build());
    }

    private String extractChecksSummary(final IssuesStatistics statistics) {
        return String.format("## %d issues in total:\n"
                        + "- ### %d new issues\n"
                        + "- ### %d outstanding Issues\n"
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

    private List<ChecksAnnotation> extractChecksAnnotations(final Report issues) {
        return issues.stream()
                .map(issue -> new ChecksAnnotationBuilder()
                        .withPath(issue.getFileName())
                        .withTitle(issue.getType())
                        .withAnnotationLevel(ChecksAnnotationLevel.WARNING)
                        .withMessage(issue.getSeverity() + ": " + issue.getMessage())
                        .withStartLine(issue.getLineStart())
                        .withEndLine(issue.getLineEnd())
                        .withStartColumn(issue.getColumnStart())
                        .withEndColumn(issue.getColumnEnd())
                        .withRawDetails(issue.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
}
