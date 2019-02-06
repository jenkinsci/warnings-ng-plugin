package io.jenkins.plugins.analysis.core.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

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
    }

    @Test
    void shouldPassIfSizesAreZero() {
        Logger logger = new Logger();

        IssuesStatisticsBuilder builder = new IssuesStatisticsBuilder();

        QGate qualityGate = new QGate();
        qualityGate.add(1, (IssuesStatistics issues) -> issues.getTotalSize(), QualityGateStatus.WARNING);

        assertThat(qualityGate.evaluate(builder.build(), logger)).isEqualTo(QualityGateStatus.INACTIVE);
    }

    private static class Logger implements FormattedLogger {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void print(final String format, final Object... args) {
            messages.add(String.format(format, args));
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}