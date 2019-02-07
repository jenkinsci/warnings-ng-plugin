package io.jenkins.plugins.analysis.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.util.QGate.GateStrength;
import io.jenkins.plugins.analysis.core.util.QualityGate.FormattedLogger;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the class {@link QGate}.
 *
 * @author Ullrich Hafner
 */
class QGateTest {
    @Test
    void shouldBeInactiveIfGatesAreEmpty() {
        Logger logger = new Logger();
        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        QGate qualityGate = new QGate();

        assertThat(qualityGate.evaluate(builder.build(), logger)).isEqualTo(QualityGateStatus.INACTIVE);

        assertThat(logger.getMessages()).containsExactly(
                "-> INACTIVE - No quality gate defined");
    }

    @Test
    void shouldPassIfSizesAreZero() {
        Logger logger = new Logger();

        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        QGate qualityGate = new QGate();

        qualityGate.add(1, IssuesStatistics::getTotalSize, "Total number of issues", GateStrength.WARNING);
        assertThat(qualityGate.evaluate(builder.build(), logger)).isEqualTo(QualityGateStatus.PASSED);
        assertThat(logger.getMessages()).containsExactly(
                "-> PASSED - Total number of issues: 0 - Quality Gate: 1");

        logger.clear();
        qualityGate.add(1, IssuesStatistics::getNewSize, "Number of new issues", GateStrength.WARNING);
        assertThat(qualityGate.evaluate(builder.build(), logger)).isEqualTo(QualityGateStatus.PASSED);
        assertThat(logger.getMessages()).containsExactly(
                "-> PASSED - Total number of issues: 0 - Quality Gate: 1",
                "-> PASSED - Number of new issues: 0 - Quality Gate: 1");
    }

    @Test
    void shouldEvaluateAllProperties() {
        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        evaluateQualityGateFor(builder, builder::setTotalSize, IssuesStatistics::getTotalSize);
        evaluateQualityGateFor(builder, builder::setTotalHighSize, IssuesStatistics::getTotalHighSize);
        evaluateQualityGateFor(builder, builder::setTotalNormalSize, IssuesStatistics::getTotalNormalSize);
        evaluateQualityGateFor(builder, builder::setTotalLowSize, IssuesStatistics::getTotalLowSize);

        evaluateQualityGateFor(builder, builder::setDeltaSize, IssuesStatistics::getDeltaSize);
        evaluateQualityGateFor(builder, builder::setDeltaHighSize, IssuesStatistics::getDeltaHighSize);
        evaluateQualityGateFor(builder, builder::setDeltaNormalSize, IssuesStatistics::getDeltaNormalSize);
        evaluateQualityGateFor(builder, builder::setDeltaLowSize, IssuesStatistics::getDeltaLowSize);

        evaluateQualityGateFor(builder, builder::setNewSize, IssuesStatistics::getNewSize);
        evaluateQualityGateFor(builder, builder::setNewHighSize, IssuesStatistics::getNewHighSize);
        evaluateQualityGateFor(builder, builder::setNewNormalSize, IssuesStatistics::getNewNormalSize);
        evaluateQualityGateFor(builder, builder::setNewLowSize, IssuesStatistics::getNewLowSize);
    }

    private void evaluateQualityGateFor(final IssuesStatisticsBuilder builder,
            final Function<Integer, IssuesStatisticsBuilder> setter,
            final Function<IssuesStatistics, Integer> getter) {
        builder.clear();

        Logger logger = new Logger();

        QGate qualityGate = new QGate();

        String name = "Total number of issues";
        qualityGate.add(1, getter, name, GateStrength.WARNING);

        assertThat(qualityGate.evaluate(builder.build(), logger)).isEqualTo(QualityGateStatus.PASSED);
        assertThat(logger.getMessages()).containsExactly("-> PASSED - " + name + ": 0 - Quality Gate: 1");

        logger.clear();
        setter.apply(1);
        assertThat(qualityGate.evaluate(builder.build(), logger)).isEqualTo(QualityGateStatus.WARNING);
        assertThat(logger.getMessages()).containsExactly("-> WARNING - " + name + ": 1 - Quality Gate: 1");
    }

    @Test
    void shouldFailIfSizeIsEqual() {
        Logger logger = new Logger();

        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        QGate qualityGate = new QGate();

        qualityGate.add(1, IssuesStatistics::getTotalSize, "Total number of issues", GateStrength.WARNING);
        assertThat(qualityGate.evaluate(builder.setTotalSize(1).build(), logger)).isEqualTo(QualityGateStatus.WARNING);
        assertThat(logger.getMessages()).containsExactly(
                "-> WARNING - Total number of issues: 1 - Quality Gate: 1");

        logger.clear();
        qualityGate.add(1, IssuesStatistics::getNewSize, "Number of new issues", GateStrength.WARNING);
        assertThat(qualityGate.evaluate(builder.setNewSize(1).build(), logger)).isEqualTo(QualityGateStatus.WARNING);
        assertThat(logger.getMessages()).containsExactly(
                "-> WARNING - Total number of issues: 1 - Quality Gate: 1",
                "-> WARNING - Number of new issues: 1 - Quality Gate: 1");
    }

    @Test
    void shouldOverrideWarningWithFailure() {
        Logger logger = new Logger();

        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        QGate qualityGate = new QGate();

        qualityGate.add(1, IssuesStatistics::getTotalSize, "Total number of issues", GateStrength.WARNING);
        qualityGate.add(2, IssuesStatistics::getTotalSize, "Total number of issues", GateStrength.FAILURE);
        assertThat(qualityGate.evaluate(builder.setTotalSize(1).build(), logger)).isEqualTo(QualityGateStatus.WARNING);
        assertThat(logger.getMessages()).containsExactly(
                "-> WARNING - Total number of issues: 1 - Quality Gate: 1",
                "-> PASSED - Total number of issues: 1 - Quality Gate: 2");

        logger.clear();
        assertThat(qualityGate.evaluate(builder.setTotalSize(2).build(), logger)).isEqualTo(QualityGateStatus.FAILED);
        assertThat(logger.getMessages()).containsExactly(
                "-> WARNING - Total number of issues: 2 - Quality Gate: 1",
                "-> FAILED - Total number of issues: 2 - Quality Gate: 2");

        QGate other = new QGate();

        other.add(2, IssuesStatistics::getTotalSize, "Total number of issues", GateStrength.FAILURE);
        other.add(1, IssuesStatistics::getTotalSize, "Total number of issues", GateStrength.WARNING);
        assertThat(other.evaluate(builder.setTotalSize(1).build(), logger)).isEqualTo(QualityGateStatus.WARNING);
        assertThat(other.evaluate(builder.setTotalSize(2).build(), logger)).isEqualTo(QualityGateStatus.FAILED);
    }

    /**
     * Logger for the tests that provides a way verify and clear the messages.
     */
    private static class Logger implements FormattedLogger {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void print(final String format, final Object... args) {
            messages.add(String.format(format, args));
        }

        List<String> getMessages() {
            return messages;
        }

        void clear() {
            messages.clear();
        }
    }
}