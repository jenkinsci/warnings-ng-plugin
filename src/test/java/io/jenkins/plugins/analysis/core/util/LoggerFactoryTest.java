package io.jenkins.plugins.analysis.core.util;

import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static org.mockito.Mockito.*;

import hudson.plugins.analysis.core.Settings;

/**
 * Tests the class {@link LoggerFactory}.
 *
 * @author Ullrich Hafner
 */
class LoggerFactoryTest {
    private static final String LOG_MESSAGE = "Hello Logger!";
    private static final String TOOL_NAME = "test";
    private static final String EXPECTED_TOOL_PREFIX = "[test]";
    private static final String FIRST_MESSAGE = "One";
    private static final String SECOND_MESSAGE = "Two";

    /**
     * Verifies that all logger methods print to the print stream if the quiet mode in Jenkins global configuration has
     * been disabled.
     */
    @Test
    void shouldReturnLoggerIfQuietModeIsDeactivated() {
        Settings settings = createSettings(false);

        LoggerFactory factory = new LoggerFactory(settings);
        PrintStream printStream = mock(PrintStream.class);
        Logger logger = factory.createLogger(printStream, TOOL_NAME);

        logger.log(LOG_MESSAGE);

        verify(printStream).println(EXPECTED_TOOL_PREFIX + " " + LOG_MESSAGE);

        Logger loggerWithBraces = factory.createLogger(printStream, EXPECTED_TOOL_PREFIX);

        loggerWithBraces.log(LOG_MESSAGE);

        verify(printStream, times(2)).println(EXPECTED_TOOL_PREFIX + " " + LOG_MESSAGE);

        logger.logEachLine(emptyList());

        verifyNoMoreInteractions(printStream);

        logger.logEachLine(singletonList(FIRST_MESSAGE));

        verify(printStream).println(EXPECTED_TOOL_PREFIX + " " + FIRST_MESSAGE);

        logger.logEachLine(asList(FIRST_MESSAGE, SECOND_MESSAGE));
        verify(printStream, times(2)).println(EXPECTED_TOOL_PREFIX + " " + FIRST_MESSAGE);
        verify(printStream).println(EXPECTED_TOOL_PREFIX + " " + SECOND_MESSAGE);
    }

    /**
     * Verifies that all logger methods do not print anything if the quiet mode in Jenkins global configuration has been
     * enabled.
     */
    @Test
    void shouldReturnNullLoggerIfQuietModeIsEnabled() {
        Settings settings = createSettings(true);

        LoggerFactory factory = new LoggerFactory(settings);
        PrintStream printStream = mock(PrintStream.class);
        Logger logger = factory.createLogger(printStream, TOOL_NAME);

        logger.log(LOG_MESSAGE);

        verifyZeroInteractions(printStream);

        logger.logEachLine(emptyList());
        logger.logEachLine(singletonList(FIRST_MESSAGE));
        logger.logEachLine(asList(FIRST_MESSAGE, SECOND_MESSAGE));

        verifyZeroInteractions(printStream);
    }

    private Settings createSettings(final boolean value) {
        Settings settings = mock(Settings.class);
        when(settings.getQuietMode()).thenReturn(value);
        return settings;
    }
}
