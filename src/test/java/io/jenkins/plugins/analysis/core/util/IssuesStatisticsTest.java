package io.jenkins.plugins.analysis.core.util;

import java.util.Map;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.SerializableTest;

import io.jenkins.plugins.analysis.core.util.IssuesStatistics.StatisticProperties;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link IssuesStatistics}.
 *
 * @author Ullrich Hafner
 */
class IssuesStatisticsTest extends SerializableTest<IssuesStatistics> {
    @Test
    void shouldCreateStatistics() {
        IssuesStatistics statistics = createSerializable();

        assertThat(StatisticProperties.TOTAL.get(statistics)).isEqualTo(1 + 2 + 3 + 4);
        assertThat(StatisticProperties.NEW.get(statistics)).isEqualTo(5 + 6 + 7 + 8);
        assertThat(StatisticProperties.DELTA.get(statistics)).isEqualTo(9 + 10 + 11 + 12);
        assertThat(StatisticProperties.FIXED.get(statistics)).isEqualTo(13);

        assertThat((Map<Severity, Integer>) statistics.getTotalSizePerSeverity().toMap()).contains(
                entry(Severity.ERROR, 1),
                entry(Severity.WARNING_HIGH, 2),
                entry(Severity.WARNING_NORMAL, 3),
                entry(Severity.WARNING_LOW, 4));
    }

    @Override
    protected IssuesStatistics createSerializable() {
        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        builder.setTotalErrorSize(1)
                .setTotalHighSize(2)
                .setTotalNormalSize(3)
                .setTotalLowSize(4)
                .setNewErrorSize(5)
                .setNewHighSize(6)
                .setNewNormalSize(7)
                .setNewLowSize(8)
                .setDeltaErrorSize(9)
                .setDeltaHighSize(10)
                .setDeltaNormalSize(11)
                .setDeltaLowSize(12)
                .setFixedSize(13);

        return builder.build();
    }
}
