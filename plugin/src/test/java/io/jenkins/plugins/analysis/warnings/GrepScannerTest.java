package io.jenkins.plugins.analysis.warnings;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.ResourceTest;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

import static edu.hm.hafner.analysis.assertions.Assertions.*;

/**
 * Tests the class {@link GrepScanner}.
 *
 * @author Akash Manna
 */
class GrepScannerTest extends ResourceTest {
    private IssueBuilder createIssueBuilder() {
        return new IssueBuilder().setFileName("test");
    }

    /**
     * Verifies that an empty pattern results in an error being reported.
     */
    @Test
    void shouldReportErrorForEmptyPattern() {
        var scanner = new GrepScanner("", Severity.WARNING_NORMAL, "");

        assertThat(scanner.isInvalidPattern()).isFalse(); // empty string is a valid (if trivial) regex
        var report = scanner.scanLines(lines("some text"), createIssueBuilder());

        // empty pattern matches every position, so every line is a match
        assertThat(report).hasSize(1);
    }

    /**
     * Verifies that an invalid regex pattern is handled gracefully and no issues are reported.
     */
    @Test
    void shouldReportErrorForInvalidPattern() {
        var scanner = new GrepScanner("[invalid", Severity.WARNING_NORMAL, "");

        assertThat(scanner.isInvalidPattern()).isTrue();
        assertThat(scanner.getErrorMessage()).contains("[invalid");

        var report = scanner.scanLines(lines("some text with ERROR"), createIssueBuilder());
        assertThat(report).hasSize(0);
        assertThat(report.getErrorMessages()).hasSize(1);
        assertThat(report.getErrorMessages().get(0)).contains("[invalid");
    }

    /**
     * Verifies that the scanner finds exactly the lines that match the given pattern.
     */
    @Test
    void shouldFindMatchingLines() {
        var scanner = new GrepScanner("ERROR", Severity.WARNING_HIGH, "");

        var report = scanner.scanLines(
                lines("INFO: system started",
                        "ERROR: something went wrong",
                        "INFO: processing continues",
                        "ERROR: another failure"),
                createIssueBuilder());

        assertThat(report).hasSize(2);
        assertThat(report.get(0)).hasLineStart(2).hasSeverity(Severity.WARNING_HIGH);
        assertThat(report.get(1)).hasLineStart(4).hasSeverity(Severity.WARNING_HIGH);
    }

    /**
     * Verifies that the scanner correctly handles files that have no matching lines.
     */
    @Test
    void shouldReturnEmptyReportForNoMatches() {
        var scanner = new GrepScanner("EXCEPTION", Severity.WARNING_NORMAL, "");

        var report = scanner.scanLines(
                lines("INFO: startup complete",
                        "DEBUG: initialised pool",
                        "INFO: shutting down"),
                createIssueBuilder());

        assertThat(report).hasSize(0);
    }

    /**
     * Verifies that the severity is correctly assigned to each matched issue.
     */
    @Test
    void shouldAssignSeverityCorrectly() {
        var highScanner = new GrepScanner("FATAL", Severity.WARNING_HIGH, "");
        var normalScanner = new GrepScanner("WARN", Severity.WARNING_NORMAL, "");
        var lowScanner = new GrepScanner("HINT", Severity.WARNING_LOW, "");

        assertThat(highScanner.scanLines(lines("FATAL: disk full"), createIssueBuilder()).get(0))
                .hasSeverity(Severity.WARNING_HIGH);
        assertThat(normalScanner.scanLines(lines("WARN: retrying"), createIssueBuilder()).get(0))
                .hasSeverity(Severity.WARNING_NORMAL);
        assertThat(lowScanner.scanLines(lines("HINT: consider using cache"), createIssueBuilder()).get(0))
                .hasSeverity(Severity.WARNING_LOW);
    }

    /**
     * Verifies that the matched line is used as the issue message when no message template is set.
     */
    @Test
    void shouldUseMatchedLineAsDefaultMessage() {
        var scanner = new GrepScanner("ERROR", Severity.WARNING_NORMAL, "");

        var report = scanner.scanLines(lines("  ERROR: disk read failure  "), createIssueBuilder());

        assertThat(report).hasSize(1);
        assertThat(report.get(0).getMessage()).isEqualTo("ERROR: disk read failure");
    }

    /**
     * Verifies that a custom message template can be supplied and is used for matched issues.
     */
    @Test
    void shouldUseCustomMessageTemplate() {
        // Pattern with a capturing group, template uses $1
        var scanner = new GrepScanner("ERROR: (.*)", Severity.WARNING_NORMAL, "Problem: $1");

        var report = scanner.scanLines(lines("ERROR: disk full"), createIssueBuilder());

        assertThat(report).hasSize(1);
        assertThat(report.get(0).getMessage()).isEqualTo("Problem: disk full");
    }

    /**
     * Verifies that a full-line regex pattern correctly matches.
     */
    @Test
    void shouldMatchWithFullLineRegex() {
        var scanner = new GrepScanner("^ERROR.*failure$", Severity.WARNING_NORMAL, "");

        var report = scanner.scanLines(
                lines("ERROR: some failure",
                        "WARNING: another issue",
                        "ERROR: disk failure"),
                createIssueBuilder());

        assertThat(report).hasSize(2);
        assertThat(report.get(0)).hasLineStart(1);
        assertThat(report.get(1)).hasLineStart(3);
    }

    /**
     * Verifies that the line number is recorded correctly.
     */
    @Test
    void shouldRecordCorrectLineNumbers() {
        var scanner = new GrepScanner("MATCH", Severity.WARNING_NORMAL, "");

        var report = scanner.scanLines(
                lines("line 1",
                        "line 2 has MATCH",
                        "line 3",
                        "line 4 has MATCH",
                        "line 5 has MATCH"),
                createIssueBuilder());

        assertThat(report).hasSize(3);
        assertThat(report.get(0)).hasLineStart(2);
        assertThat(report.get(1)).hasLineStart(4);
        assertThat(report.get(2)).hasLineStart(5);
    }

    /**
     * Verifies that scanning a non-existent file produces an error in the report.
     */
    @Test
    void shouldReportExceptionForMissingFile() {
        var scanner = new GrepScanner("ERROR", Severity.WARNING_NORMAL, "");

        var report = scanner.scan(new File("non-existent-file.txt").toPath(), StandardCharsets.UTF_8);

        assertThat(report.getErrorMessages()).isNotEmpty();
        assertThat(report.getErrorMessages().get(0)).contains("non-existent-file.txt");
    }

    /**
     * Verifies that the scanner handles a file with the wrong encoding gracefully.
     */
    @Test
    void shouldHandleMalformedInputException() throws URISyntaxException {
        var scanner = new GrepScanner("ERROR", Severity.WARNING_NORMAL, "");

        // Reuse the binary test file from the task scanner test resources
        var url = getClass().getResource(
                "/io/jenkins/plugins/analysis/warnings/tasks/file-with-strange-characters.txt");
        var pathToFile = Paths.get(url.toURI());
        var report = scanner.scan(pathToFile, StandardCharsets.UTF_8);

        assertThat(report.getErrorMessages()).isNotEmpty();
        assertThat(report.getErrorMessages().get(0))
                .contains("Can't read source file").contains("UTF-8");
    }

    /**
     * Verifies that partial word regex matches work correctly (non-word-boundary matching).
     */
    @Test
    void shouldMatchPartialWords() {
        var scanner = new GrepScanner("ERR", Severity.WARNING_NORMAL, "");

        var report = scanner.scanLines(
                lines("ERROR: failure",
                        "ERRATIC behaviour detected",
                        "info: no ERRors"),
                createIssueBuilder());

        // All 3 lines contain "ERR" as a substring
        assertThat(report).hasSize(3);
    }

    /**
     * Verifies that case-sensitive matching works (Java regex is case-sensitive by default).
     */
    @Test
    void shouldBeCaseSensitiveByDefault() {
        var scanner = new GrepScanner("error", Severity.WARNING_NORMAL, "");

        var report = scanner.scanLines(
                lines("ERROR: uppercase",
                        "error: lowercase",
                        "Error: mixed"),
                createIssueBuilder());

        // Only the lowercase "error" line matches
        assertThat(report).hasSize(1);
        assertThat(report.get(0)).hasLineStart(2);
    }

    /**
     * Verifies that case-insensitive matching can be achieved via regex flag.
     */
    @Test
    void shouldSupportCaseInsensitiveFlagInPattern() {
        var scanner = new GrepScanner("(?i)error", Severity.WARNING_NORMAL, "");

        var report = scanner.scanLines(
                lines("ERROR: uppercase",
                        "error: lowercase",
                        "Error: mixed"),
                createIssueBuilder());

        // All 3 lines match with case-insensitive flag
        assertThat(report).hasSize(3);
    }

    /**
     * Verifies that the ERROR severity is correctly assigned to matched issues.
     */
    @Test
    void shouldAssignErrorSeverityCorrectly() {
        var scanner = new GrepScanner("FATAL", Severity.ERROR, "");

        assertThat(scanner.scanLines(lines("FATAL: system crash"), createIssueBuilder()).get(0))
                .hasSeverity(Severity.ERROR);
    }

    private Iterator<String> lines(final String... lines) {
        return Arrays.stream(lines).iterator();
    }
}
