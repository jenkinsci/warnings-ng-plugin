package hudson.plugins.analysis.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.PrintStream;

import org.junit.Test;

import hudson.plugins.analysis.core.GlobalSettings;

/**
 * FIXME: Document type LoggerFactoryTest.
 *
 * @author Ulli Hafner
 */
public class LoggerFactoryTest {

    /**
     * Tests if a "true" PluginLogger is created, when the Quiet Mode is deactivated.
     */
    @Test
    public void quietModeDeactivated() {
        //Given
        GlobalSettings.DescriptorImpl settings = mock(GlobalSettings.DescriptorImpl.class);
        LoggerFactory lf = new LoggerFactory(settings);

        //When
        when(settings.getQuiet()).thenReturn(false);
        PluginLogger logger = lf.createLogger(mock(PrintStream.class), "");

        //Then
        assertFalse("LogMode is not Quiet but LoggerFactory creates a NullLogger!", logger instanceof NullLogger);
    }

    /**
     * Tests if a NullLogger is created, when the Quiet Mode is active.
     */
    @Test
    public void quietModeActivated() {
        //Given
        GlobalSettings.DescriptorImpl settings = mock(GlobalSettings.DescriptorImpl.class);
        LoggerFactory lf = new LoggerFactory(settings);

        //When
        when(settings.getQuiet()).thenReturn(true);
        PluginLogger logger = lf.createLogger(mock(PrintStream.class), "");

        //Then
        assertTrue("LogMode is Quiet but LoggerFactory creates not a NullLogger!", logger instanceof NullLogger);
    }
}
