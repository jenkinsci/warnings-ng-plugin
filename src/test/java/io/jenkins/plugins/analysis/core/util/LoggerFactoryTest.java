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
    @Test
    void shouldReturnLoggerIfQuietModeIsDeactivated() {
        Settings settings = createSettings(false);

        LoggerFactory factory = new LoggerFactory(settings);
        PrintStream printStream = mock(PrintStream.class);
        Logger logger = factory.createLogger(printStream, "test");

        logger.log("Hello World");

        verify(printStream).println("[test] Hello World");

        Logger loggerWithBraces = factory.createLogger(printStream, "[test]");

        loggerWithBraces.log("Hello World");

        verify(printStream, times(2)).println("[test] Hello World");

        logger.logEachLine(emptyList());

        verifyNoMoreInteractions(printStream);

        logger.logEachLine(singletonList("One"));

        verify(printStream).println("[test] One");

        logger.logEachLine(asList("One", "Two"));
        verify(printStream, times(2)).println("[test] One");
        verify(printStream).println("[test] Two");

    }

    @Test
    void shouldReturnNullLoggerIfQuietModeIsEnabled() {
        Settings settings = createSettings(true);

        LoggerFactory factory = new LoggerFactory(settings);
        PrintStream printStream = mock(PrintStream.class);
        Logger logger = factory.createLogger(printStream, "test");

        logger.log("Hello World");

        verifyZeroInteractions(printStream);

        logger.logEachLine(emptyList());
        logger.logEachLine(singletonList("One"));
        logger.logEachLine(asList("One", "Two"));

        verifyZeroInteractions(printStream);
    }

    private Settings createSettings(final boolean value) {
        Settings settings = mock(Settings.class);
        when(settings.getQuietMode()).thenReturn(value);
        return settings;
    }
}
