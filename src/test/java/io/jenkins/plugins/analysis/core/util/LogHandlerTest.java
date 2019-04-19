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

    LogHandler createMockedLogHandler(final PrintStream printStream) {
        TaskListener taskListener = mock(TaskListener.class);
        when(taskListener.getLogger()).thenReturn(printStream);
        return spy(new LogHandler(taskListener, LOG_HANDLER_NAME));
    }

    @Test
    void shouldLogInfoAndErrorMessage() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        LogHandler logHandler = createMockedLogHandler(printStream);

        Report report = new Report();
        report.logInfo(MESSAGE);
        report.logError(MESSAGE);
        logHandler.log(report);

        // Assert if the output has the following structure:
        //      "[LOG_HANDLER_NAME] [-ERROR-] MESSAGE\n
        //      [LOG_HANDLER_NAME] MESSAGE\n"
        assertThat(outputStream.toString()).isEqualTo(String.format(
                "[%s] [-ERROR-] %s" + System.getProperty("line.separator")
                        + "[%s] %s" + System.getProperty("line.separator"),
                LOG_HANDLER_NAME, MESSAGE, LOG_HANDLER_NAME, MESSAGE));
    }

    @Test
    void shouldLogFormattedMessage() {
        final String logFormat = "Skipping '%s'";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        LogHandler logHandler = createMockedLogHandler(printStream);

        logHandler.log(logFormat, MESSAGE);

        assertThat(outputStream.toString()).isEqualTo(
                String.format("[%s] " + logFormat + System.getProperty("line.separator"), LOG_HANDLER_NAME, MESSAGE)
        );
    }

}