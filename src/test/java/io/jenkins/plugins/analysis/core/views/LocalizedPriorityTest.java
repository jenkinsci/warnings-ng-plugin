package io.jenkins.plugins.analysis.core.views;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Priority;
import static org.assertj.core.api.Assertions.*;

import hudson.plugins.analysis.Messages;

/**
 * Unit test for {@link LocalizedPriority}.
 *
 * @author Deniz Mardin
 * @author Frank Christian Geyer
 */
class LocalizedPriorityTest {

    /**
     * Verifies that the expected string is correct. This test case checks for positive results.
     */
    @Test
    void shouldGetLocalizedStringHighPriorityPositiveTestCase() {
        assertThat(LocalizedPriority.getLocalizedString(Priority.HIGH)).isEqualTo(Messages.Priority_High());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for negative results.
     */
    @Test
    void shouldGetLocalizedStringHighPriorityNegativeTestCase() {
        assertThat(LocalizedPriority.getLocalizedString(Priority.HIGH)).isNotEqualTo(Messages.Priority_Low());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for positive results.
     */
    @Test
    void shouldGetLocalizedStringNormalPriorityPositiveTestCase() {
        assertThat(LocalizedPriority.getLocalizedString(Priority.NORMAL)).isEqualTo(Messages.Priority_Normal());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for negative results.
     */
    @Test
    void shouldGetLocalizedStringNormalPriorityNegativeTestCase() {
        assertThat(LocalizedPriority.getLocalizedString(Priority.NORMAL)).isNotEqualTo(Messages.Priority_High());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for positive results.
     */
    @Test
    void shouldGetLocalizedStringLowPriorityPositiveTestCase() {
        assertThat(LocalizedPriority.getLocalizedString(Priority.LOW)).isEqualTo(Messages.Priority_Low());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for negative results.
     */
    @Test
    void shouldGetLocalizedStringLowPriorityNegativeTestCase() {
        assertThat(LocalizedPriority.getLocalizedString(Priority.LOW)).isNotEqualTo(Messages.Priority_Normal());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for positive results.
     */
    @Test
    void shouldGetLocalizedStringNormalPriorityForNullPositiveTestCase() {
        assertThat(LocalizedPriority.getLocalizedString(null)).isEqualTo(Messages.Priority_Normal());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for negative results.
     */
    @Test
    void shouldGetLocalizedStringNormalPriorityForNullNegativeTestCase() {
        assertThat(LocalizedPriority.getLocalizedString(null)).isNotEqualTo(Messages.Priority_High());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for positive results.
     */
    @Test
    void shouldGetLongLocalizedStringHighPriorityPositiveTestCase() {
        assertThat(LocalizedPriority.getLongLocalizedString(Priority.HIGH)).isEqualTo(Messages.HighPriority());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for negative results.
     */
    @Test
    void shouldGetLongLocalizedStringHighPriorityNegativeTestCase() {
        assertThat(LocalizedPriority.getLongLocalizedString(Priority.HIGH)).isNotEqualTo(Messages.LowPriority());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for positive results.
     */
    @Test
    void shouldGetLongLocalizedStringNormalPriorityPositiveTestCase() {
        assertThat(LocalizedPriority.getLongLocalizedString(Priority.NORMAL)).isEqualTo(Messages.NormalPriority());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for negative results.
     */
    @Test
    void shouldGetLongLocalizedStringNormalPriorityNegativeTestCase() {
        assertThat(LocalizedPriority.getLongLocalizedString(Priority.NORMAL)).isNotEqualTo(Messages.LowPriority());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for positive results.
     */
    @Test
    void shouldGetLongLocalizedStringLowPriorityPositiveTestCase() {
        assertThat(LocalizedPriority.getLongLocalizedString(Priority.LOW)).isEqualTo(Messages.LowPriority());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for negative results.
     */
    @Test
    void shouldGetLongLocalizedStringLowPriorityNegativeTestCase() {
        assertThat(LocalizedPriority.getLongLocalizedString(Priority.LOW)).isNotEqualTo(Messages.NormalPriority());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for positive results.
     */
    @Test
    void shouldGetLongLocalizedStringNormalPriorityForNullPositiveTestCase() {
        assertThat(LocalizedPriority.getLongLocalizedString(null)).isEqualTo(Messages.NormalPriority());
    }

    /**
     * Verifies that the expected string is correct. This test case checks for negative results.
     */
    @Test
    void shouldGetLongLocalizedStringNormalPriorityForNullNegativeTestCase() {
        assertThat(LocalizedPriority.getLongLocalizedString(null)).isNotEqualTo(Messages.HighPriority());
    }

}