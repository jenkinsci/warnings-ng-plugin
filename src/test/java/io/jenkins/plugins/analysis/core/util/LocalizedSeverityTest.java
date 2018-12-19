package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.testutil.LocalizedMessagesTest;

import static edu.hm.hafner.analysis.assertj.Assertions.*;
import static io.jenkins.plugins.analysis.core.util.LocalizedSeverity.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link LocalizedSeverity}.
 *
 * @author Anna-Maria Hardi
 */
class LocalizedSeverityTest extends LocalizedMessagesTest {
    private static final String SEVERITY_NAME = "Severity Name";

    @Test
    void shouldProvideLocalizedSeverity() {
        assertThat(getLocalizedString(Severity.ERROR)).isEqualTo(
                Messages.Severity_Short_Error());
        assertThat(getLocalizedString(Severity.WARNING_HIGH)).isEqualTo(
                Messages.Severity_Short_High());
        assertThat(getLocalizedString(Severity.WARNING_NORMAL)).isEqualTo(
                Messages.Severity_Short_Normal());
        assertThat(getLocalizedString(Severity.WARNING_LOW)).isEqualTo(
                Messages.Severity_Short_Low());
        assertThat(getLocalizedString(createSeverity())).isEqualTo(SEVERITY_NAME);
    }

    @Test
    void shouldProvideLongLocalizedSeverity() {
        assertThat(getLongLocalizedString(Severity.ERROR)).isEqualTo(
                Messages.Severity_Long_Error());
        assertThat(getLongLocalizedString(Severity.WARNING_HIGH)).isEqualTo(
                Messages.Severity_Long_High());
        assertThat(getLongLocalizedString(Severity.WARNING_NORMAL)).isEqualTo(
                Messages.Severity_Long_Normal());
        assertThat(getLongLocalizedString(Severity.WARNING_LOW)).isEqualTo(Messages.Severity_Long_Low());
        assertThat(getLongLocalizedString(createSeverity())).isEqualTo(SEVERITY_NAME);
    }

    private Severity createSeverity() {
        Severity severity = mock(Severity.class);
        when(severity.getName()).thenReturn(SEVERITY_NAME);
        return severity;
    }
}