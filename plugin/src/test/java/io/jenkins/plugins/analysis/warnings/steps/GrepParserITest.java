package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.GrepParser;
import io.jenkins.plugins.util.QualityGateStatus;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests for the {@link GrepParser} tool.
 *
 * @author Akash Manna
 * @see <a href="https://issues.jenkins.io/browse/JENKINS-53014">JENKINS-53014</a>
 */
@org.junitpioneer.jupiter.Issue("JENKINS-53014")
class GrepParserITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String ERROR_PATTERN = "ERROR";
    private static final String WARN_PATTERN = "WARN(?:ING)?";

    /**
     * Verifies that the GrepParser correctly counts the number of matched lines in a workspace file.
     */
    @Test
    void shouldCountMatchesInSingleFile() {
        var project = createFreeStyleProject();
        createFileInWorkspace(project, "application.log",
                "INFO: application started\n"
                        + "ERROR: null pointer exception\n"
                        + "INFO: processing request\n"
                        + "ERROR: connection timeout\n"
                        + "INFO: shutdown complete\n");

        var parser = createGrepParser(ERROR_PATTERN, "**/*.log", "", Severity.WARNING_HIGH.getName());
        enableWarnings(project, parser);

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(2);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
        assertThat(result.getIssues().getSizeOf(Severity.WARNING_HIGH)).isEqualTo(2);
        assertThat(result.getIssues().getSizeOf(Severity.WARNING_NORMAL)).isEqualTo(0);
    }

    /**
     * Verifies that the GrepParser reports zero issues when there are no matches.
     */
    @Test
    void shouldReportZeroMatchesWhenNoLineMatches() {
        var project = createFreeStyleProject();
        createFileInWorkspace(project, "clean.log",
                "INFO: all good\n"
                        + "DEBUG: initialised\n"
                        + "INFO: done\n");

        var parser = createGrepParser(ERROR_PATTERN, "**/*.log", "", Severity.WARNING_NORMAL.getName());
        enableWarnings(project, parser);

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
    }

    /**
     * Verifies that the GrepParser uses the correct severity for matched issues.
     */
    @Test
    void shouldAssignCorrectSeverity() {
        var project = createFreeStyleProject();
        createFileInWorkspace(project, "app.log",
                "ERROR: something failed\n"
                        + "INFO: working\n");

        // Test HIGH severity
        var highParser = createGrepParser(ERROR_PATTERN, "**/*.log", "", Severity.WARNING_HIGH.getName());
        enableWarnings(project, highParser);
        var highResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(highResult.getIssues().getSizeOf(Severity.WARNING_HIGH)).isEqualTo(1);
        assertThat(highResult.getIssues().getSizeOf(Severity.WARNING_NORMAL)).isEqualTo(0);
    }

    /**
     * Verifies that the GrepParser respects the include file pattern.
     */
    @Test
    void shouldRespectIncludePattern() {
        var project = createFreeStyleProject();
        createFileInWorkspace(project, "app.log", "ERROR: from log\n");
        createFileInWorkspace(project, "app.txt", "ERROR: from txt\n");

        // Only scan *.log files
        var parser = createGrepParser(ERROR_PATTERN, "**/*.log", "", Severity.WARNING_NORMAL.getName());
        enableWarnings(project, parser);

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Only one match: the one in app.log
        assertThat(result).hasTotalSize(1);
        assertThat(result.getIssues().get(0).getFileName()).endsWith("app.log");
    }

    /**
     * Verifies that the GrepParser respects the exclude file pattern.
     */
    @Test
    void shouldRespectExcludePattern() {
        var project = createFreeStyleProject();
        createFileInWorkspace(project, "app.log", "ERROR: from log\n");
        createFileInWorkspace(project, "skip.log", "ERROR: should be excluded\n");

        // Exclude skip.log
        var parser = createGrepParser(ERROR_PATTERN, "**/*.log", "", Severity.WARNING_NORMAL.getName());
        parser.setExcludePattern("**/skip.log");
        enableWarnings(project, parser);

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Only one match: the one in app.log
        assertThat(result).hasTotalSize(1);
        assertThat(result.getIssues().get(0).getFileName()).endsWith("app.log");
    }

    /**
     * Verifies that the GrepParser logs informational messages during scanning.
     */
    @Test
    void shouldProduceInfoMessages() {
        var project = createFreeStyleProject();
        createFileInWorkspace(project, "app.log", "ERROR: failure\n");

        var parser = createGrepParser(ERROR_PATTERN, "**/*.log", "", Severity.WARNING_NORMAL.getName());
        enableWarnings(project, parser);

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result.getInfoMessages()).anySatisfy(
                message -> assertThat(message).contains("found 1 files that will be scanned"));
        assertThat(result.getInfoMessages()).anySatisfy(
                message -> assertThat(message).contains("Found a total of 1 grep matches"));
    }

    /**
     * Verifies that the GrepParser uses the tool's display name and ID correctly.
     */
    @Test
    void shouldHaveCorrectToolIdAndName() {
        var project = createFreeStyleProject();
        createFileInWorkspace(project, "app.log", "ERROR: failure\n");

        var parser = createGrepParser(ERROR_PATTERN, "**/*.log", "", Severity.WARNING_NORMAL.getName());
        enableWarnings(project, parser);

        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);
        var action = getResultAction(build);

        assertThat(action.getId()).isEqualTo("grep");
        assertThat(action.getDisplayName()).contains("Grep Parser");
    }

    /**
     * Verifies that the GrepParser supports regular expression patterns with groups.
     */
    @Test
    void shouldSupportRegexWithGroups() {
        var project = createFreeStyleProject();
        createFileInWorkspace(project, "app.log",
                "2024-01-01 ERROR - disk full\n"
                        + "2024-01-02 INFO - all good\n"
                        + "2024-01-03 ERROR - timeout\n");

        // Pattern with a group to capture the error description
        var parser = createGrepParser(".*ERROR - (.*)", "**/*.log", "Detected: $1",
                Severity.WARNING_NORMAL.getName());
        enableWarnings(project, parser);

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(2);
        assertThat(result.getIssues().get(0).getMessage()).isEqualTo("Detected: disk full");
        assertThat(result.getIssues().get(1).getMessage()).isEqualTo("Detected: timeout");
    }

    /**
     * Verifies that the GrepParser handles multiple log files and aggregates results.
     */
    @Test
    void shouldScanMultipleFiles() {
        var project = createFreeStyleProject();
        createFileInWorkspace(project, "app1.log", "ERROR: failure in app1\nINFO: ok\n");
        createFileInWorkspace(project, "app2.log", "INFO: all good\nERROR: failure in app2\nERROR: second failure\n");

        var parser = createGrepParser(ERROR_PATTERN, "**/*.log", "", Severity.WARNING_NORMAL.getName());
        enableWarnings(project, parser);

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // 1 from app1.log + 2 from app2.log = 3 total
        assertThat(result).hasTotalSize(3);
    }

    /**
     * Verifies that when no files match the include pattern the result has zero issues and no error.
     */
    @Test
    void shouldHandleNoMatchingFiles() {
        var project = createFreeStyleProject();
        createFileInWorkspace(project, "app.log", "ERROR: something\n");

        // Pattern that will find no files
        var parser = createGrepParser(ERROR_PATTERN, "**/non-existent-*.log", "", Severity.WARNING_NORMAL.getName());
        enableWarnings(project, parser);

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        
        // No error messages should be produced for a directory with no matching files
        assertThat(result.getErrorMessages()).isEmpty();
    }

    /**
     * Verifies that the GrepParser can be used with LOW severity.
     */
    @Test
    void shouldSupportLowSeverity() {
        var project = createFreeStyleProject();
        createFileInWorkspace(project, "app.log", "HINT: consider optimization\n");

        var parser = createGrepParser("HINT", "**/*.log", "", Severity.WARNING_LOW.getName());
        enableWarnings(project, parser);

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(1).hasTotalLowPrioritySize(1);
    }

    /**
     * Verifies that the GrepParser correctly processes case-insensitive patterns via regex flag.
     */
    @Test
    void shouldSupportCaseInsensitivePatternViaRegexFlag() {
        var project = createFreeStyleProject();
        createFileInWorkspace(project, "app.log",
                "error: lowercase\n"
                        + "ERROR: uppercase\n"
                        + "Error: mixed\n");

        var parser = createGrepParser("(?i)error", "**/*.log", "", Severity.WARNING_NORMAL.getName());
        enableWarnings(project, parser);

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(3);
    }

    // ==================== Helper methods ====================

    private GrepParser createGrepParser(final String regexp, final String includePattern,
            final String message, final String severity) {
        var parser = new GrepParser();
        parser.setRegexp(regexp);
        parser.setIncludePattern(includePattern);
        parser.setMessage(message);
        parser.setSeverity(severity);
        return parser;
    }
}
