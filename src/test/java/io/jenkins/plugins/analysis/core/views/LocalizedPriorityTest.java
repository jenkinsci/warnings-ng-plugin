package io.jenkins.plugins.analysis.core.views;


import edu.hm.hafner.analysis.Priority;
import org.junit.jupiter.api.Test;
import static edu.hm.hafner.analysis.assertj.Assertions.*;

/**
 * Tests the class {@link LocalizedPriority}.
 *
 * @author Anna-Maria Hardi
 */
class LocalizedPriorityTest {
    @Test
    void priorityIsHigh() {
        String actualResult = LocalizedPriority.getLocalizedString(Priority.HIGH);
        assertThat(actualResult).isNotEmpty();
        assertThat(actualResult).contains("Hoch");
        assertThat(actualResult).inUnicode();
    }
}