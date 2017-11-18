package hudson.plugins.analysis.util;

import java.io.PrintStream;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import hudson.plugins.analysis.core.Settings;

/**
 * TestCases for {@link PluginLogger}.
 *
 * @author Sebastian Seidl
 */
public class LoggerFactoryTest {

    /**
     * Tests if a "true" PluginLogger is created, when the Quiet Mode is deactivated.
     */
    @Test
    public void quietModeDeactivated() {
        // Given
        Settings settings = mock(Settings.class);
        LoggerFactory loggerFactory = new LoggerFactory(settings);

        // When
        when(settings.getQuietMode()).thenReturn(false);
        PluginLogger logger = loggerFactory.createLogger(mock(PrintStream.class), "");

        // Then
        assertFalse("LogMode is not Quiet but LoggerFactory creates a NullLogger!", logger instanceof NullLogger);
    }

    /**
     * Tests if a NullLogger is created, when the Quiet Mode is active.
     */
    @Test
    public void quietModeActivated() {
        // Given
        Settings settings = mock(Settings.class);
        LoggerFactory lf = new LoggerFactory(settings);

        // When
        when(settings.getQuietMode()).thenReturn(true);
        PluginLogger logger = lf.createLogger(mock(PrintStream.class), "");

        // Then
        assertTrue("LogMode is Quiet but LoggerFactory creates not a NullLogger!", logger instanceof NullLogger);
    }
}
