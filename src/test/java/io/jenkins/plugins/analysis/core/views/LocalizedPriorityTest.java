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

    @Test
    void priorityIsHighLong() {
        String actualResult = LocalizedPriority.getLongLocalizedString(Priority.HIGH);
        assertThat(actualResult).isNotEmpty();
        assertThat(actualResult).contains("Hohe Priorität");
        assertThat(actualResult).inUnicode();
    }

    @Test
    void priorityIsLow() {
        String actualResult = LocalizedPriority.getLocalizedString(Priority.LOW);
        assertThat(actualResult).isNotEmpty();
        assertThat(actualResult).contains("Niedrig");
        assertThat(actualResult).inUnicode();
    }

    @Test
    void priorityIsLowLong() {
        String actualResult = LocalizedPriority.getLongLocalizedString(Priority.LOW);
        assertThat(actualResult).isNotEmpty();
        assertThat(actualResult).contains("Niedrige Priorität");
        assertThat(actualResult).inUnicode();
    }

    @Test
    void priorityIsNormal() {
        String actualResult = LocalizedPriority.getLocalizedString(Priority.NORMAL);
        assertThat(actualResult).isNotEmpty();
        assertThat(actualResult).contains("Normal");
        assertThat(actualResult).inUnicode();
    }

    @Test
    void priorityIsNormalLong() {
        String actualResult = LocalizedPriority.getLongLocalizedString(Priority.NORMAL);
        assertThat(actualResult).isNotEmpty();
        assertThat(actualResult).contains("Normale Priorität");
        assertThat(actualResult).inUnicode();
    }

    @Test
    void priorityIsNull() {
        String actualResult = LocalizedPriority.getLocalizedString(null);
        assertThat(actualResult).contains("Normal");
    }

    @Test
    void priorityIsNullLong() {
        String actualResult = LocalizedPriority.getLongLocalizedString(null);
        assertThat(actualResult).contains("Normale Priorität");
    }

}