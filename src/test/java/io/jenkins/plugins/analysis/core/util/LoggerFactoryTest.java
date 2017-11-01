package io.jenkins.plugins.analysis.core.util;

import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
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
        Settings settings = mock(Settings.class);
        when(settings.getQuietMode()).thenReturn(false);

        LoggerFactory factory = new LoggerFactory(settings);
        Logger logger = factory.createLogger(mock(PrintStream.class), "test");

        assertThat(logger).isInstanceOf(AnalysisLogger.class);
    }

    @Test
    void shouldReturnNullLoggerIfQuietModeIsEnabled() {
        Settings settings = mock(Settings.class);
        when(settings.getQuietMode()).thenReturn(true);

        LoggerFactory factory = new LoggerFactory(settings);
        Logger logger = factory.createLogger(mock(PrintStream.class), "test");

        assertThat(logger).isInstanceOf(NullLogger.class);
    }
}