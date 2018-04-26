package io.jenkins.plugins.analysis.core.views;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Priority;
import static edu.hm.hafner.analysis.assertj.Assertions.*;
import io.jenkins.plugins.analysis.core.testutil.LocalizedMessagesTest;

import hudson.plugins.analysis.Messages;

/**
 * Tests the class {@link LocalizedPriority}.
 *
 * @author Anna-Maria Hardi
 */
class LocalizedPriorityTest extends LocalizedMessagesTest {
    @Test
    void testLocalizedStringHighLowNormalNull() {
        String actualResult;
        actualResult = LocalizedPriority.getLocalizedString(Priority.HIGH);
        assertThat(actualResult).isEqualTo(Messages.Priority_High());
        actualResult = LocalizedPriority.getLocalizedString(Priority.LOW);
        assertThat(actualResult).isEqualTo(Messages.Priority_Low());
        actualResult = LocalizedPriority.getLocalizedString(Priority.NORMAL);
        assertThat(actualResult).isEqualTo(Messages.Priority_Normal());
        actualResult = LocalizedPriority.getLocalizedString(null);
        assertThat(actualResult).isEqualTo(Messages.Priority_Normal());
    }

    @Test
    void testLongLocalizedStringHighLowNormalNull() {
        String actualResult;
        actualResult = LocalizedPriority.getLongLocalizedString(Priority.HIGH);
        assertThat(actualResult).isEqualTo(Messages.HighPriority());
        actualResult = LocalizedPriority.getLongLocalizedString(Priority.LOW);
        assertThat(actualResult).isEqualTo(Messages.LowPriority());
        actualResult = LocalizedPriority.getLongLocalizedString(Priority.NORMAL);
        assertThat(actualResult).isEqualTo(Messages.NormalPriority());
        actualResult = LocalizedPriority.getLongLocalizedString(null);
        assertThat(actualResult).isEqualTo(Messages.NormalPriority());
    }

}