package io.jenkins.plugins.analysis.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.jenkins.plugins.analysis.core.util.QualityGate.FormattedLogger;

/**
 * FIXME: comment class.
 *
 * @author Ullrich Hafner
 */
public class QGate {
    private final List<Gate> gates = new ArrayList<>();

    /**
     * Enforces this quality gate for the specified run.
     *
     * @param report
     *         the report to evaluate
     * @param logger
     *         the logger that reports the passed and failed quality gate thresholds
     *
     * @return result of the evaluation, expressed by a build state
     */
    public QualityGateStatus evaluate(final IssuesStatistics report, final FormattedLogger logger) {
        if (gates.isEmpty()) {
            logger.print("-> INACTIVE - No quality gate defined");

            return QualityGateStatus.INACTIVE;
        }

        QualityGateStatus status = QualityGateStatus.PASSED;

        for (Gate gate : gates) {
            int actualSize = gate.resultMethod.apply(report);
            if (actualSize >= gate.size) {
                logger.print("-> %s - %s: %d - Quality Gate: %d", gate.status, gate.name, actualSize, gate.size);
                if (gate.status.isWorseThan(status)) {
                    status = gate.status;
                }
            }
            else {
                logger.print("-> PASSED - %s: %d - Quality Gate: %d", gate.name, actualSize, gate.size);
            }
        }

        return status;
    }

    public void add(final int size, final Function<IssuesStatistics, Integer> resultMethod, final String name,
            final GateStrength status) {
        gates.add(new Gate(size, resultMethod, name, status));
    }

    public enum GateStrength {
        WARNING(QualityGateStatus.WARNING),

        FAILURE(QualityGateStatus.FAILED);

        private QualityGateStatus status;

        GateStrength(final QualityGateStatus status) {
            this.status = status;
        }

        public QualityGateStatus getStatus() {
            return status;
        }

        public int getOrder() {
            return -status.ordinal();
        }
    }

    private static class Gate {
        private final int size;
        private final Function<IssuesStatistics, Integer> resultMethod;
        private final String name;
        private final QualityGateStatus status;

        Gate(final int size, final Function<IssuesStatistics, Integer> resultMethod,
                final String name, final GateStrength strength) {
            this.size = size;
            this.resultMethod = resultMethod;
            this.name = name;
            status = strength.status;
        }
    }
}
