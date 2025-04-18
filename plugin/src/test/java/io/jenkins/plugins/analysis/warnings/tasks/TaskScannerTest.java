package io.jenkins.plugins.analysis.warnings.tasks;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.ResourceTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.MatcherMode;

import static edu.hm.hafner.analysis.assertions.Assertions.*;

/**
 * Tests the class {@link TaskScanner}.
 *
 * @author Ullrich Hafner
 */
class TaskScannerTest extends ResourceTest {
    private static final String FIXME = "FIXME";
    private static final String CASE_TEST_FILE = "tasks-case-test.txt";
    private static final String PRIORITY_HIGH_MESSAGE = "here another task with priority HIGH";
    private static final String PRIORITY_NORMAL_MESSAGE = "here we have a task with priority NORMAL";
    private static final String FILE_WITH_TASKS = "file-with-tasks.txt";
    private static final IssueBuilder ISSUE_BUILDER = new IssueBuilder();

    @Test
    void shouldReportFileExceptionError() {
        var scanner = new TaskScannerBuilder().build();

        var report = scanner.scan(new File("").toPath(), StandardCharsets.UTF_8);

        assertThat(report.getErrorMessages()).contains("Exception while reading the source code file '':");
    }

    @Test
    void shouldHandleMalformedInputException() {
        var scanner = new TaskScannerBuilder().build();

        var pathToFile = getResourceAsFile("file-with-strange-characters.txt");
        var report = scanner.scan(pathToFile, StandardCharsets.UTF_8);

        assertThat(report.getErrorMessages()).isNotEmpty().contains("Can't read source file '"
                + pathToFile
                + "', defined encoding 'UTF-8' seems to be wrong");
    }

    @Test
    void shouldReportErrorIfPatternIsInvalid() {
        var scanner = new TaskScannerBuilder().setHighTasks("[)")
                .setMatcherMode(MatcherMode.REGEXP_MATCH)
                .build();

        var report = scanner.scanTasks(read(FILE_WITH_TASKS), ISSUE_BUILDER);

        assertThat(report).hasSize(0);
        var errorMessage = "Specified pattern is an invalid regular expression: '[)': "
                + "'Unclosed character class near index 1";
        assertThat(report.getErrorMessages()).hasSize(1);
        assertThat(report.getErrorMessages().get(0)).startsWith(errorMessage);

        assertThat(scanner.isInvalidPattern()).isTrue();
        assertThat(scanner.getErrors()).startsWith(errorMessage);
    }

    /**
     * Parses tasks using a regular expression.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-17225">Issue 17225</a>
     */
    @Test
    void shouldParseRegularExpressionsIssue17225() {
        var tasks = new TaskScannerBuilder()
                .setHighTasks("^.*(TODO(?:[0-9]*))(.*)$")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.REGEXP_MATCH)
                .build()
                .scanTasks(read("regexp.txt"), ISSUE_BUILDER);

        assertThat(tasks).hasSize(5);
        assertThat(tasks.get(0)).hasSeverity(Severity.WARNING_HIGH)
                .hasType("TODO1")
                .hasLineStart(1)
                .hasMessage("erstes");
        assertThat(tasks.get(1)).hasSeverity(Severity.WARNING_HIGH)
                .hasType("TODO2")
                .hasLineStart(2)
                .hasMessage("zweites");
        assertThat(tasks.get(2)).hasSeverity(Severity.WARNING_HIGH)
                .hasType("TODO3")
                .hasLineStart(3)
                .hasMessage("drittes");
        assertThat(tasks.get(3)).hasSeverity(Severity.WARNING_HIGH)
                .hasType("TODO4")
                .hasLineStart(4)
                .hasMessage("viertes");
        assertThat(tasks.get(4)).hasSeverity(Severity.WARNING_HIGH)
                .hasType("TODO20")
                .hasLineStart(5)
                .hasMessage("zwanzigstes");
    }

    /**
     * Gracefully handles patterns that contain optional groups.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-64622">Issue 64622</a>
     */
    @Test @org.junitpioneer.jupiter.Issue("JENKINS-64622")
    void shouldHandleEmptyMatchWithRegExp() {
        var tasks = new TaskScannerBuilder()
                .setHighTasks("(a)?(b)?.*")
                .setMatcherMode(MatcherMode.REGEXP_MATCH)
                .build()
                .scanTasks(Arrays.asList("-", "-").iterator(), ISSUE_BUILDER);

        assertThat(tasks).hasSize(2);
    }

    /**
     * Parses a warning log with characters in different locale.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-22744">Issue 22744</a>
     */
    @Test @org.junitpioneer.jupiter.Issue("JENKINS-22744")
    void issue22744() {
        var tasks = new TaskScannerBuilder()
                .setHighTasks("FIXME")
                .setNormalTasks("TODO")
                .setLowTasks("")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read("issue22744.java", "windows-1251"), ISSUE_BUILDER);

        assertThat(tasks).hasSize(2);
        assertThat(tasks.get(0)).hasSeverity(Severity.WARNING_HIGH)
                .hasType("FIXME")
                .hasLineStart(4)
                .hasMessage("\u0442\u0435\u0441\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 Jenkins");
        assertThat(tasks.get(1)).hasSeverity(Severity.WARNING_NORMAL)
                .hasType("TODO")
                .hasLineStart(5)
                .hasMessage(
                        "\u043f\u0440\u0438\u043c\u0435\u0440 \u043a\u043e\u043c\u043c\u0435\u043d\u0442\u0430\u0440\u0438\u044f \u043d\u0430 \u0440\u0443\u0441\u0441\u043a\u043e\u043c");
    }

    /**
     * Parses a warning log with !!! and !!!! warnings.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-12782">Issue 12782</a>
     */
    @Test
    void issue12782() {
        var tasks = new TaskScannerBuilder()
                .setHighTasks("!!!!!")
                .setNormalTasks("!!!")
                .setLowTasks("")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read("issue12782.txt"), ISSUE_BUILDER);

        assertThat(tasks).hasSize(3);
    }

    /**
     * Checks whether we find tasks at word boundaries.
     */
    @Test
    void shouldScanFileWithWords() {
        var tasks = new TaskScannerBuilder()
                .setHighTasks("WARNING")
                .setNormalTasks("TODO")
                .setLowTasks("@todo")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read("tasks-words-test.txt"), ISSUE_BUILDER);

        assertThat(tasks).hasSize(12);
        assertThatReportHasSeverities(tasks,
                0, 0, 7, 5);
    }

    /**
     * Checks case sensitivity.
     */
    @Test
    void shouldIgnoreCase() {
        verifyOneTaskWhenCheckingCase("todo", 25);
        verifyOneTaskWhenCheckingCase("ToDo", 27);
    }

    private void verifyOneTaskWhenCheckingCase(final String tag, final int lineNumber) {
        var tasks = new TaskScannerBuilder()
                .setNormalTasks(tag)
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read(CASE_TEST_FILE), ISSUE_BUILDER);

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0))
                .hasSeverity(Severity.WARNING_NORMAL)
                .hasType(tag)
                .hasLineStart(lineNumber)
                .hasMessage("");
    }

    @Test
    void shouldIgnoreCaseInSource() {
        var tasks = new TaskScannerBuilder()
                .setNormalTasks("todo")
                .setCaseMode(CaseMode.IGNORE_CASE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read(CASE_TEST_FILE), ISSUE_BUILDER);

        assertThat(tasks).hasSize(9);
        for (Issue task : tasks) {
            assertThat(task).hasType("TODO");
        }
    }

    @Test
    void shouldIgnoreCaseInTag() {
        var tasks = new TaskScannerBuilder()
                .setNormalTasks("Todo, TodoS")
                .setCaseMode(CaseMode.IGNORE_CASE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read(CASE_TEST_FILE), ISSUE_BUILDER);

        assertThat(tasks).hasSize(12);
        for (Issue task : tasks) {
            assertThat(task.getType()).startsWith("TODO");
        }
    }

    /**
     * Checks whether we find the two task in the test file.
     */
    @Test
    void shouldUseDefaults() {
        var tasks = new TaskScannerBuilder().setHighTasks("FIXME")
                .setNormalTasks("TODO")
                .setLowTasks("@deprecated")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read(FILE_WITH_TASKS), ISSUE_BUILDER);

        assertThat(tasks).hasSize(2);
        assertThatReportHasSeverities(tasks, 0, 1, 1, 0);
        assertThat(tasks.get(0)).hasMessage(PRIORITY_NORMAL_MESSAGE);
        assertThat(tasks.get(1)).hasMessage(PRIORITY_HIGH_MESSAGE);
    }

    /**
     * Checks whether we find one high priority task in the test file.
     */
    @Test
    void shouldFindHighPriority() {
        var tasks = new TaskScannerBuilder().setHighTasks(FIXME)
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read(FILE_WITH_TASKS), ISSUE_BUILDER);

        assertThat(tasks).hasSize(1);
        assertThatReportHasSeverities(tasks, 0, 1, 0, 0);
    }

    /**
     * Checks whether we correctly strip whitespace from the message.
     */
    @Test
    void shouldIgnoreSpaceInTags() {
        var tasks = new TaskScannerBuilder().setHighTasks(" FIXME , TODO ")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read(FILE_WITH_TASKS), ISSUE_BUILDER);

        assertThat(tasks).hasSize(2);
        assertThatReportHasSeverities(tasks, 0, 2, 0, 0);
    }

    /**
     * Checks whether we find two high priority tasks with different identifiers in the test file.
     */
    @Test
    void shouldHaveTwoItemsWithHighPriority() {
        var tasks = new TaskScannerBuilder().setHighTasks("FIXME,TODO")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read(FILE_WITH_TASKS), ISSUE_BUILDER);

        assertThat(tasks).hasSize(2);
        assertThatReportHasSeverities(tasks, 0, 2, 0, 0);
    }

    /**
     * Checks whether we set the type of the task to the actual tag.
     */
    @Test
    void shouldIdentifyTags() {
        var text = "FIXME: this is a fixme";
        var high = new TaskScannerBuilder()
                .setHighTasks("FIXME,TODO")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(readFromString(text), ISSUE_BUILDER);

        assertThat(high).hasSize(1);
        assertThat(high.get(0)).hasType(FIXME);

        var normal = new TaskScannerBuilder()
                .setNormalTasks("XXX, HELP, FIXME, TODO")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(readFromString(text), ISSUE_BUILDER);

        assertThat(normal).hasSize(1);
        assertThat(normal.get(0)).hasType(FIXME);
    }

    private Iterator<String> readFromString(final String text) {
        return new BufferedReader(new StringReader(text)).lines().iterator();
    }

    /**
     * Checks whether we find all priority tasks in the test file.
     */
    @Test
    void shouldScanAllPriorities() {
        var tasks = new TaskScannerBuilder().setHighTasks(FIXME)
                .setNormalTasks("FIXME,TODO")
                .setLowTasks("TODO")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read(FILE_WITH_TASKS), ISSUE_BUILDER);

        assertThat(tasks).hasSize(4);
        assertThatReportHasSeverities(tasks, 0, 1, 2, 1);
    }

    /**
     * Checks whether we find no task in the test file.
     */
    @Test
    void shouldScanFileWithoutTasks() {
        var tasks = new TaskScannerBuilder().setHighTasks("FIXME")
                .setNormalTasks("TODO")
                .setLowTasks("@deprecated")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read("file-without-tasks.txt"), ISSUE_BUILDER);

        assertThat(tasks).hasSize(0);
    }

    /**
     * Checks whether ignoring parts of a file works.
     */
    @Test
    void shouldIgnoreItsOwnConfigurationWithIgnoreSectionMark() {
        var tasks = new TaskScannerBuilder().setHighTasks("FIXME")
                .setNormalTasks("TODO")
                .setLowTasks("REVIEW")
                .setHighTasks("FIXME")
                .setCaseMode(CaseMode.CASE_SENSITIVE)
                .setMatcherMode(MatcherMode.STRING_MATCH)
                .build()
                .scanTasks(read("file-with-tasks-and-ignore-section.txt"), ISSUE_BUILDER);

        assertThat(tasks).hasSize(0);
    }

    private Iterator<String> read(final String fileName) {
        try (Stream<String> file = asStream(fileName)) {
            return file.collect(Collectors.toList()).iterator();
        }
    }

    private Iterator<String> read(final String fileName, final String charset) {
        try (Stream<String> file = asStream(fileName, Charset.forName(charset))) {
            return asIterator(file);
        }
    }

    private Iterator<String> asIterator(final Stream<String> file) {
        return file.collect(Collectors.toList()).iterator();
    }

    private void assertThatReportHasSeverities(final Report report, final int expectedSizeError,
            final int expectedSizeHigh, final int expectedSizeNormal, final int expectedSizeLow) {
        assertThat(report.getSizeOf(Severity.ERROR)).isEqualTo(expectedSizeError);
        assertThat(report.getSizeOf(Severity.WARNING_HIGH)).isEqualTo(expectedSizeHigh);
        assertThat(report.getSizeOf(Severity.WARNING_NORMAL)).isEqualTo(expectedSizeNormal);
        assertThat(report.getSizeOf(Severity.WARNING_LOW)).isEqualTo(expectedSizeLow);
    }
}
