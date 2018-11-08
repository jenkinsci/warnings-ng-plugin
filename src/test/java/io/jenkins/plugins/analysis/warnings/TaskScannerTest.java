package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import static edu.hm.hafner.analysis.assertj.Assertions.*;
import edu.hm.hafner.util.ResourceTest;

/**
 * Tests the class {@link TaskScanner}.
 */
class TaskScannerTest extends ResourceTest {
    /** Fixme tags. */
    private static final String FIXME = "FIXME";
    /** Filename for tests. */
    private static final String TEST_FILE = "tasks-case-test.txt";
    /** High priority. */
    private static final String PRIORITY_HIGH = "here another task with priority HIGH";
    /** Normal priority. */
    private static final String PRIORITY_NORMAL = "here we have a task with priority NORMAL";
    /** Test file. */
    private static final String FILE_WITH_TASKS = "file-with-tasks.txt";

    /**
     * Parses tasks using a regular expression.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-17225">Issue 17225</a>
     */
    @Test
    void testRegularExpressionsIssue17225() {
        Report result = scan("regexp.txt", 5, "^.*(TODO(?:[0-9]*))(.*)$", null, "", false, true);
        int line = 1;
        verifyTask(result.get(line - 1), Severity.WARNING_HIGH, "TODO1", line++, "erstes");
        verifyTask(result.get(line - 1), Severity.WARNING_HIGH, "TODO2", line++, "zweites");
        verifyTask(result.get(line - 1), Severity.WARNING_HIGH, "TODO3", line++, "drittes");
        verifyTask(result.get(line - 1), Severity.WARNING_HIGH, "TODO4", line++, "viertes");
        verifyTask(result.get(line - 1), Severity.WARNING_HIGH, "TODO20", line, "zwanzigstes");
    }

    /**
     * Parses tasks using a regular expression.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-17225">Issue 17225</a>
     */
    @Test
    void testRegularExpressionAsTag() {
        Report result = scan("regexp.txt", 5, "^.*(TODO(?:[0-9]*))(.*)$", null, "", false, true);
        int line = 1;
        verifyTask(result.get(line - 1), Severity.WARNING_HIGH, "TODO1", line++, "erstes");
        verifyTask(result.get(line - 1), Severity.WARNING_HIGH, "TODO2", line++, "zweites");
        verifyTask(result.get(line - 1), Severity.WARNING_HIGH, "TODO3", line++, "drittes");
        verifyTask(result.get(line - 1), Severity.WARNING_HIGH, "TODO4", line++, "viertes");
        verifyTask(result.get(line - 1), Severity.WARNING_HIGH, "TODO20", line, "zwanzigstes");
    }

    /**
     * Parses a warning log with characters in different locale.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-22744">Issue 22744</a>
     */
    @Test
    void issue22744() throws IOException {
        InputStream file = asInputStream("tasks/" + "issue22744.java");
        InputStreamReader reader = new InputStreamReader(file, "windows-1251");

        Report result = scan(reader, 2, "FIXME", "TODO", "", false, false);

        Issue task = result.get(0);
        verifyTask(task, Severity.WARNING_HIGH, "FIXME", 4, "\u0442\u0435\u0441\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 Jenkins");
        task = result.get(1);
        verifyTask(task, Severity.WARNING_NORMAL, "TODO", 5, "\u043f\u0440\u0438\u043c\u0435\u0440 \u043a\u043e\u043c\u043c\u0435\u043d\u0442\u0430\u0440\u0438\u044f \u043d\u0430 \u0440\u0443\u0441\u0441\u043a\u043e\u043c");
    }

    private void verifyTask(final Issue task, final Severity priority, final String tag, final int line, final String message) {
        assertThat(task).hasSeverity(priority).hasType(tag).hasLineStart(line).hasMessage(message);
    }

    /**
     * Parses a warning log with !!! and !!!! warnings.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-12782">Issue 12782</a>
     */
    @Test
    void issue12782() {
        scan("issue12782.txt", 3, "!!!!!", "!!!", "", false, false);
    }

    /**
     * Checks whether we find tasks at word boundaries.
     */
    @Test
    void scanFileWithWords() {
        Report result = scan("tasks-words-test.txt", 12, "WARNING", "TODO", "@todo", false, false);

        assertThat(result).hasSeverities(0, 0, 7, 5);
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
        Report result = scan(TEST_FILE, 1, null, tag, null, false, false);
        Issue task = result.get(0);
        verifyTask(task, Severity.WARNING_NORMAL, tag, lineNumber, "");
    }

    @Test
    void shouldIgnoreCaseInSource() {
        Report result = scan(TEST_FILE, 9, null, "todo", null, true, false);
        for (Issue task : result) {
            assertThat(task).hasType("TODO");
        }
    }

    @Test
    public void shouldIgnoreCaseInTag() {
        scan(TEST_FILE, 12, null, "Todo, TodoS", null, true, false);
    }

    /**
     * Checks whether we find the two task in the test file.
     */
    @Test
    void shouldUseDefaults() {
        Report result = scan(FILE_WITH_TASKS, 2);

        assertThat(result.get(0)).hasMessage(PRIORITY_NORMAL);
        assertThat(result.get(1)).hasMessage(PRIORITY_HIGH);

        assertThat(result).hasSeverities(0, 1, 1, 0);
    }

    /**
     * Checks whether we find one high priority task in the test file.
     */
    @Test
    void shouldFindHighPriority() {
        Report result = scan(FILE_WITH_TASKS, 1, FIXME, null, null, false, false);

        assertThat(result).hasSeverities(0, 1, 0, 0);
    }

    /**
     * Checks whether we correctly strip whitespace from the message.
     */
    @Test
    void shouldIgnoreSpaceInTags() {
        Report result = scan(FILE_WITH_TASKS, 2, " FIXME , TODO ", null, null, false, false);

        assertThat(result).hasSeverities(0, 2, 0, 0);
    }

    /**
     * Checks whether we find two high priority tasks with different identifiers in the test file.
     */
    @Test
    void shouldHaveTwoItemsWithHighPriority() {
        Report result = scan(FILE_WITH_TASKS, 2, "FIXME,TODO", null, null, false, false);

        assertThat(result).hasSeverities(0, 2, 0, 0);
    }

    /**
     * Checks whether we set the type of the task to the actual tag.
     */
    @Test
    void shouldIdentifyTags() {
        String text = "FIXME: this is a fixme";
        Report result = scan(new StringReader(text), 1, "FIXME,TODO", null, null, false, false);
        Issue task = result.get(0);
        assertThat(task).hasType(FIXME);

        result = scan(new StringReader(text), 1, null, "XXX, HELP, FIXME, TODO", null, false, false);

        assertThat(task).hasType(FIXME);
    }

    /**
     * Checks whether we find all priority tasks in the test file.
     */
    @Test
    void shouldScanAllPriorities() {
        Report result = scan(FILE_WITH_TASKS, 4, FIXME, "FIXME,TODO", "TODO", false, false);

        assertThat(result).hasSeverities(0, 1, 2, 1);
    }

    /**
     * Checks whether we find no task in the test file.
     */
    @Test
    void shouldScanFileWithoutTasks() {
        Report result = scan("file-without-tasks.txt", 0);

        assertThat(result).hasSize(0);
    }

    private Report scan(final String fileName, final int expectedNumberOfTasks,
                                  final String high, final String normal, final String low,
                                  final boolean ignoreCase, final boolean asRegexp) {
        InputStream file = asInputStream("tasks/" + fileName);
        InputStreamReader reader = new InputStreamReader(file);

        return scan(reader, expectedNumberOfTasks, high, normal, low, ignoreCase, asRegexp);
    }

    private Report scan(final Reader reader, final int expectedNumberOfTasks,
                                  final String high, final String normal, final String low,
                                  final boolean ignoreCase, final boolean asRegexp) {
        try {
            Report tasks = new TaskScanner(high, normal, low, ignoreCase, asRegexp).scan(reader);
            assignProperties(tasks);
            assertThat(tasks).hasSize(expectedNumberOfTasks);

            return tasks;
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private Report scan(final String fileName, final int expectedNumberOfTasks) {
        InputStream file = asInputStream("tasks/" + fileName);
        InputStreamReader reader = new InputStreamReader(file);

        return scan(reader, expectedNumberOfTasks);
    }

    private Report scan(final Reader reader, final int expectedNumberOfTasks) {
        try {
            Report tasks = new TaskScanner().scan(reader);
            assignProperties(tasks);
            assertThat(tasks).hasSize(expectedNumberOfTasks);
            
            return tasks;
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Assigns properties to all tasks.
     *
     * @param result
     *      the tasks to assign the properties for
     */
    private void assignProperties(final Report result) {
        for (Issue task : result) {
            task.setFileName("Path/To/TestFile");
            task.setPackageName("Package");
            task.setModuleName("Module");
        }
    }
}

