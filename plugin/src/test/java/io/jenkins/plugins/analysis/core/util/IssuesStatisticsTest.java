package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.SerializableTest;

import java.util.Map;
import java.util.NoSuchElementException;

import io.jenkins.plugins.analysis.core.util.IssuesStatistics.StatisticProperties;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link IssuesStatistics}.
 *
 * @author Ullrich Hafner
 */
class IssuesStatisticsTest extends SerializableTest<IssuesStatistics> {
    @Test
    void shouldCreateStatistics() {
        var statistics = createSerializable();

        assertThat(StatisticProperties.TOTAL.get(statistics)).isEqualTo(1 + 2 + 3 + 4);
        assertThat(StatisticProperties.TOTAL_ERROR.get(statistics)).isEqualTo(1);
        assertThat(StatisticProperties.TOTAL_HIGH.get(statistics)).isEqualTo(2);
        assertThat(StatisticProperties.TOTAL_NORMAL.get(statistics)).isEqualTo(3);
        assertThat(StatisticProperties.TOTAL_LOW.get(statistics)).isEqualTo(4);
        assertThat(StatisticProperties.TOTAL_MODIFIED.get(statistics)).isEqualTo(14);

        assertThat(statistics.getTotalSizeOf(Severity.ERROR)).isEqualTo(1);
        assertThat(statistics.getTotalSizeOf(Severity.WARNING_HIGH)).isEqualTo(2);
        assertThat(statistics.getTotalSizeOf(Severity.WARNING_NORMAL)).isEqualTo(3);
        assertThat(statistics.getTotalSizeOf(Severity.WARNING_LOW)).isEqualTo(4);

        assertThat(StatisticProperties.NEW.get(statistics)).isEqualTo(5 + 6 + 7 + 8);
        assertThat(StatisticProperties.NEW_ERROR.get(statistics)).isEqualTo(5);
        assertThat(StatisticProperties.NEW_HIGH.get(statistics)).isEqualTo(6);
        assertThat(StatisticProperties.NEW_NORMAL.get(statistics)).isEqualTo(7);
        assertThat(StatisticProperties.NEW_LOW.get(statistics)).isEqualTo(8);
        assertThat(StatisticProperties.NEW_MODIFIED.get(statistics)).isEqualTo(15);

        assertThat(statistics.getNewSizeOf(Severity.ERROR)).isEqualTo(5);
        assertThat(statistics.getNewSizeOf(Severity.WARNING_HIGH)).isEqualTo(6);
        assertThat(statistics.getNewSizeOf(Severity.WARNING_NORMAL)).isEqualTo(7);
        assertThat(statistics.getNewSizeOf(Severity.WARNING_LOW)).isEqualTo(8);

        assertThat(StatisticProperties.DELTA.get(statistics)).isEqualTo(9 + 10 + 11 + 12);
        assertThat(StatisticProperties.DELTA_ERROR.get(statistics)).isEqualTo(9);
        assertThat(StatisticProperties.DELTA_HIGH.get(statistics)).isEqualTo(10);
        assertThat(StatisticProperties.DELTA_NORMAL.get(statistics)).isEqualTo(11);
        assertThat(StatisticProperties.DELTA_LOW.get(statistics)).isEqualTo(12);

        assertThat(StatisticProperties.FIXED.get(statistics)).isEqualTo(13);

        assertThat((Map<Severity, Integer>) statistics.getTotalSizePerSeverity().toMap()).contains(
                entry(Severity.ERROR, 1),
                entry(Severity.WARNING_HIGH, 2),
                entry(Severity.WARNING_NORMAL, 3),
                entry(Severity.WARNING_LOW, 4));
    }

    @Test
    void shouldAggregateTotals() {
        var serializable = createSerializable();

        assertThat(serializable.aggregate(serializable)).hasTotalErrorSize(2)
                .hasTotalHighSize(4)
                .hasTotalNormalSize(6)
                .hasTotalLowSize(8)
                .hasTotalModifiedSize(28)
                .hasNewErrorSize(10)
                .hasNewHighSize(12)
                .hasNewNormalSize(14)
                .hasNewLowSize(16)
                .hasNewModifiedSize(30)
                .hasDeltaErrorSize(18)
                .hasDeltaHighSize(20)
                .hasDeltaNormalSize(22)
                .hasDeltaLowSize(24)
                .hasFixedSize(26);

        assertThat(serializable).isEqualTo(serializable);
    }

    @Test
    void shouldResetBuilder() {
        var builder = createBuilder();

        builder.clear();

        assertThat(builder).usingRecursiveComparison().isEqualTo(new IssuesStatisticsBuilder());
    }

    @Test
    void shouldBlub() {
        var statistics = mock(IssuesStatistics.class);

        when(statistics.getFixedSize()).thenReturn(15);

        assertThat(statistics.getFixedSize()).isEqualTo(15);
    }

    @Test
    void shouldRejectUnsupportedSeverities() {
        var statistics = createSerializable();

        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(
                () -> statistics.getNewSizeOf(null)).withMessageContaining("null");
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(
                () -> statistics.getNewSizeOf(new Severity("other"))).withMessageContaining("other");
    }

    @Override
    protected IssuesStatistics createSerializable() {
        return createBuilder().build();
    }

    private IssuesStatisticsBuilder createBuilder() {
        var builder = new IssuesStatisticsBuilder();

        builder.setTotalErrorSize(1)
                .setTotalHighSize(2)
                .setTotalNormalSize(3)
                .setTotalLowSize(4)
                .setTotalModifiedSize(14)
                .setNewErrorSize(5)
                .setNewHighSize(6)
                .setNewNormalSize(7)
                .setNewLowSize(8)
                .setNewModifiedSize(15)
                .setDeltaErrorSize(9)
                .setDeltaHighSize(10)
                .setDeltaNormalSize(11)
                .setDeltaLowSize(12)
                .setFixedSize(13);
        return builder;
    }
}
