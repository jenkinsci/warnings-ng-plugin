package io.jenkins.plugins.analysis.core.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;

import hudson.model.TaskListener;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link LogHandlerTest}.
 *
 * @author Andreas Neumeier
 */
class LogHandlerTest {
    private static final String LOG_HANDLER_NAME = "TestHandler";
    private static final String MESSAGE = "TestMessage";
    private static final TaskListener TASK_LISTENER = mock(TaskListener.class);

    @Test
    void shouldLogErrorMessage() throws UnsupportedEncodingException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        when(TASK_LISTENER.getLogger()).thenReturn(printStream);
        LogHandler logHandler = spy(new LogHandler(TASK_LISTENER, LOG_HANDLER_NAME));

        Report report = new Report();
        report.logError(MESSAGE);
        logHandler.log(report);

        String output = outputStream.toString("UTF8");
        assertThat(output).isEqualTo("[" + LOG_HANDLER_NAME + "] [-ERROR-] " + MESSAGE + "\n");
    }

    @Test
    void shouldLogInfoMessage() throws UnsupportedEncodingException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        when(TASK_LISTENER.getLogger()).thenReturn(printStream);
        LogHandler logHandler = spy(new LogHandler(TASK_LISTENER, LOG_HANDLER_NAME));

        Report report = new Report();
        report.logInfo(MESSAGE);
        logHandler.log(report);

        String output = outputStream.toString("UTF8");
        assertThat(output).isEqualTo("[" + LOG_HANDLER_NAME + "] " + MESSAGE + "\n");
    }
}