package io.jenkins.plugins.analysis.core.views;

import java.util.Locale;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Priority;

import hudson.plugins.analysis.Messages;

/**
 * Unit test for {@link LocalizedPriority}.
 *
 * @author Deniz Mardin
 * @author Frank Christian Geyer
 */
class LocalizedPriorityTest {

    /**
     * Initialize Local Settings before the test start.
     */
    @BeforeAll
    static void initializeLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    /**
     * Verifies that the expected string for the {@code getLocalizedString} string is correct. This test case checks for positive results.
     */
    @Test
    void shouldGetLocalizedStringPriorityForHighNormalLowAndNull() {
        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(LocalizedPriority.getLocalizedString(Priority.HIGH)).isEqualTo(Messages.Priority_High());
            softly.assertThat(LocalizedPriority.getLocalizedString(Priority.NORMAL))
                    .isEqualTo(Messages.Priority_Normal());
            softly.assertThat(LocalizedPriority.getLocalizedString(Priority.LOW)).isEqualTo(Messages.Priority_Low());
            softly.assertThat(LocalizedPriority.getLocalizedString(null)).isEqualTo(Messages.Priority_Normal());
        }
    }

    /**
     * Verifies that the expected string for the {@code getLongLocalizedString} method is correct. This test case checks for positive results.
     */
    @Test
    void shouldGetLongLocalizedStringPriorityHighNormalLowAndNull() {
        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(LocalizedPriority.getLongLocalizedString(Priority.HIGH))
                    .isEqualTo(Messages.HighPriority());
            softly.assertThat(LocalizedPriority.getLongLocalizedString(Priority.NORMAL))
                    .isEqualTo(Messages.NormalPriority());
            softly.assertThat(LocalizedPriority.getLongLocalizedString(Priority.LOW)).isEqualTo(Messages.LowPriority());
            softly.assertThat(LocalizedPriority.getLongLocalizedString(null)).isEqualTo(Messages.NormalPriority());
        }
    }

}
