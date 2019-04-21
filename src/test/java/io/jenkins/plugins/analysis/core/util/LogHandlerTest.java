package io.jenkins.plugins.analysis.core.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;

import hudson.model.TaskListener;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link LogHandler}.
 *
 * @author Andreas Neumeier
 */
class LogHandlerTest {
    private static final String LOG_HANDLER_NAME = "TestHandler";
    private static final String MESSAGE = "TestMessage";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private TaskListener createTaskListener(final PrintStream printStream) {
        TaskListener taskListener = mock(TaskListener.class);
        when(taskListener.getLogger()).thenReturn(printStream);
        return taskListener;
    }

    @Test
    void shouldLogInfoAndErrorMessage() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        TaskListener taskListener = createTaskListener(printStream);

        Report report = new Report();
        LogHandler logHandler = new LogHandler(taskListener, LOG_HANDLER_NAME, report);
        report.logInfo(MESSAGE);
        report.logError(MESSAGE);
        logHandler.log(report);

        assertThat(outputStream.toString()).isEqualTo(String.format(
                "[%s] [-ERROR-] %s%s"
                        + "[%s] %s%s",
                LOG_HANDLER_NAME, MESSAGE, LINE_SEPARATOR, LOG_HANDLER_NAME, MESSAGE, LINE_SEPARATOR));
    }

    @Test
    void shouldLogFormattedMessage() {
        final String logFormat = "Skipping '%s'";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        TaskListener taskListener = createTaskListener(printStream);
        LogHandler logHandler = new LogHandler(taskListener, LOG_HANDLER_NAME);

        logHandler.log(logFormat, MESSAGE);

        assertThat(outputStream.toString()).isEqualTo(
                String.format("[%s] " + logFormat + "%s", LOG_HANDLER_NAME, MESSAGE, LINE_SEPARATOR)
        );
    }
}