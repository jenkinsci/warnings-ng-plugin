package io.jenkins.plugins.analysis.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateEvaluator.FormattedLogger;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the class {@link QualityGateEvaluator}.
 *
 * @author Ullrich Hafner
 */
class QualityGateEvaluatorTest {
    @Test
    void shouldBeInactiveIfGatesAreEmpty() {
        Logger logger = new Logger();
        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        QualityGateEvaluator qualityGate = new QualityGateEvaluator();

        assertThat(qualityGate.evaluate(builder.build(), logger)).isEqualTo(QualityGateStatus.INACTIVE);

        assertThat(logger.getMessages()).containsExactly(
                "-> INACTIVE - No quality gate defined");
    }

    @Test
    void shouldHandleNegativeDeltaValues() {
        Logger logger = new Logger();

        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        QualityGateEvaluator qualityGate = new QualityGateEvaluator();

        qualityGate.add(1, QualityGateType.DELTA, QualityGateResult.UNSTABLE);
        assertThat(qualityGate.evaluate(builder.setDeltaSize(-1).build(), logger)).isEqualTo(QualityGateStatus.PASSED);
        assertThat(logger.getMessages()).containsExactly(
                "-> PASSED - " + QualityGateType.DELTA.getDisplayName() + ": -1 - Quality QualityGate: 1");
    }

    @Test
    void shouldPassIfSizesAreZero() {
        Logger logger = new Logger();

        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        QualityGateEvaluator qualityGate = new QualityGateEvaluator();

        qualityGate.add(1, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        assertThat(qualityGate.evaluate(builder.build(), logger)).isEqualTo(QualityGateStatus.PASSED);
        assertThat(logger.getMessages()).containsExactly(
                "-> PASSED - " + QualityGateType.TOTAL.getDisplayName() + ": 0 - Quality QualityGate: 1");

        logger.clear();
        qualityGate.add(1, QualityGateType.NEW, QualityGateResult.UNSTABLE);
        assertThat(qualityGate.evaluate(builder.build(), logger)).isEqualTo(QualityGateStatus.PASSED);
        assertThat(logger.getMessages()).containsExactly(
                "-> PASSED - " + QualityGateType.TOTAL.getDisplayName() + ": 0 - Quality QualityGate: 1",
                "-> PASSED - " + QualityGateType.NEW.getDisplayName() + ": 0 - Quality QualityGate: 1");
    }

    @Test
    void shouldEvaluateAllProperties() {
        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        evaluateQualityGateFor(builder, builder::setTotalSize, QualityGateType.TOTAL);
        evaluateQualityGateFor(builder, builder::setTotalErrorSize, QualityGateType.TOTAL_ERROR);
        evaluateQualityGateFor(builder, builder::setTotalHighSize, QualityGateType.TOTAL_HIGH);
        evaluateQualityGateFor(builder, builder::setTotalNormalSize, QualityGateType.TOTAL_NORMAL);
        evaluateQualityGateFor(builder, builder::setTotalLowSize, QualityGateType.TOTAL_LOW);

        evaluateQualityGateFor(builder, builder::setDeltaSize, QualityGateType.DELTA);
        evaluateQualityGateFor(builder, builder::setDeltaErrorSize, QualityGateType.DELTA_ERROR);
        evaluateQualityGateFor(builder, builder::setDeltaHighSize, QualityGateType.DELTA_HIGH);
        evaluateQualityGateFor(builder, builder::setDeltaNormalSize, QualityGateType.DELTA_NORMAL);
        evaluateQualityGateFor(builder, builder::setDeltaLowSize, QualityGateType.DELTA_LOW);

        evaluateQualityGateFor(builder, builder::setNewSize, QualityGateType.NEW);
        evaluateQualityGateFor(builder, builder::setNewErrorSize, QualityGateType.NEW_ERROR);
        evaluateQualityGateFor(builder, builder::setNewHighSize, QualityGateType.NEW_HIGH);
        evaluateQualityGateFor(builder, builder::setNewNormalSize, QualityGateType.NEW_NORMAL);
        evaluateQualityGateFor(builder, builder::setNewLowSize, QualityGateType.NEW_LOW);
    }

    private void evaluateQualityGateFor(final IssuesStatisticsBuilder builder,
            final Function<Integer, IssuesStatisticsBuilder> setter,
            final QualityGateType type) {
        builder.clear();

        Logger logger = new Logger();

        QualityGateEvaluator qualityGate = new QualityGateEvaluator();

        qualityGate.add(1, type, QualityGateResult.UNSTABLE);

        assertThat(qualityGate.evaluate(builder.build(), logger)).isEqualTo(QualityGateStatus.PASSED);
        assertThat(logger.getMessages()).containsExactly(
                "-> PASSED - " + type.getDisplayName() + ": 0 - Quality QualityGate: 1");

        logger.clear();
        setter.apply(1);
        assertThat(qualityGate.evaluate(builder.build(), logger)).isEqualTo(QualityGateStatus.WARNING);
        assertThat(logger.getMessages()).containsExactly(
                "-> WARNING - " + type.getDisplayName() + ": 1 - Quality QualityGate: 1");
    }

    @Test
    void shouldFailIfSizeIsEqual() {
        Logger logger = new Logger();

        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        QualityGateEvaluator qualityGate = new QualityGateEvaluator();

        qualityGate.add(1, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        assertThat(qualityGate.evaluate(builder.setTotalSize(1).build(), logger)).isEqualTo(QualityGateStatus.WARNING);
        assertThat(logger.getMessages()).containsExactly(
                "-> WARNING - " + QualityGateType.TOTAL.getDisplayName() + ": 1 - Quality QualityGate: 1");

        logger.clear();
        qualityGate.add(1, QualityGateType.NEW, QualityGateResult.UNSTABLE);
        assertThat(qualityGate.evaluate(builder.setNewSize(1).build(), logger)).isEqualTo(QualityGateStatus.WARNING);
        assertThat(logger.getMessages()).containsExactly(
                "-> WARNING - " + QualityGateType.TOTAL.getDisplayName() + ": 1 - Quality QualityGate: 1",
                "-> WARNING - " + QualityGateType.NEW.getDisplayName() + ": 1 - Quality QualityGate: 1");
    }

    @Test
    void shouldOverrideWarningWithFailure() {
        Logger logger = new Logger();

        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        QualityGateEvaluator qualityGate = new QualityGateEvaluator();

        qualityGate.add(1, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        qualityGate.add(2, QualityGateType.TOTAL, QualityGateResult.FAILURE);
        assertThat(qualityGate.evaluate(builder.setTotalSize(1).build(), logger)).isEqualTo(QualityGateStatus.WARNING);
        assertThat(logger.getMessages()).containsExactly(
                "-> WARNING - " + QualityGateType.TOTAL.getDisplayName() + ": 1 - Quality QualityGate: 1",
                "-> PASSED - " + QualityGateType.TOTAL.getDisplayName() + ": 1 - Quality QualityGate: 2");

        logger.clear();
        assertThat(qualityGate.evaluate(builder.setTotalSize(2).build(), logger)).isEqualTo(QualityGateStatus.FAILED);
        assertThat(logger.getMessages()).containsExactly(
                "-> WARNING - " + QualityGateType.TOTAL.getDisplayName() + ": 2 - Quality QualityGate: 1",
                "-> FAILED - " + QualityGateType.TOTAL.getDisplayName() + ": 2 - Quality QualityGate: 2");

        QualityGateEvaluator other = new QualityGateEvaluator();

        other.add(2, QualityGateType.TOTAL, QualityGateResult.FAILURE);
        other.add(1, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
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
