package io.jenkins.plugins.analysis.core.quality;

/**
 * Defines quality gates for a static analysis run.
 *
 * @author Ullrich Hafner
 */
public class QualityGate {
    private final ThresholdSet unstableThreshold;
    private final ThresholdSet failedThreshold;

    private QualityGate(final ThresholdSet unstableThreshold, final ThresholdSet failedThreshold) {
        this.unstableThreshold = unstableThreshold;
        this.failedThreshold = failedThreshold;
    }

    public ThresholdSet getUnstableThreshold() {
        return unstableThreshold;
    }

    public ThresholdSet getFailedThreshold() {
        return unstableThreshold;
    }

    static class QualitiyGateBuilder {
        private ThresholdSet unstableThreshold = new ThresholdSet(0, 0, 0, 0);
        private ThresholdSet failedThreshold = new ThresholdSet(0, 0, 0, 0);

        public QualityGate build() {
            return new QualityGate(unstableThreshold, failedThreshold);
        }

        public QualitiyGateBuilder setUnstableThreshold(final int totalThreshold, final int highThreshold, final int normalThreshold, final int lowThreshold) {
            unstableThreshold = new ThresholdSet(totalThreshold, highThreshold, normalThreshold, lowThreshold);
            return this;
        }

        public QualitiyGateBuilder setFailedThreshold(final int totalThreshold, final int highThreshold, final int normalThreshold, final int lowThreshold) {
            failedThreshold = new ThresholdSet(totalThreshold, highThreshold, normalThreshold, lowThreshold);
            return this;
        }
    }

    static class ThresholdSet{
        private final int totalThreshold;
        private final int highThreshold;
        private final int normalThreshold;
        private final int lowThreshold;

        public ThresholdSet(final int totalThreshold, final int highThreshold, final int normalThreshold, final int lowThreshold) {
            this.totalThreshold = totalThreshold;
            this.highThreshold = highThreshold;
            this.normalThreshold = normalThreshold;
            this.lowThreshold = lowThreshold;
        }

        public boolean hasTotalThreshold() {
            return totalThreshold > 0;
        }

        public int getTotalThreshold() {
            return totalThreshold;
        }

        public boolean hasHighThreshold() {
            return highThreshold > 0;
        }

        public int getHighThreshold() {
            return highThreshold;
        }

        public boolean hasNormalThreshold() {
            return normalThreshold > 0;
        }

        public int getNormalThreshold() {
            return normalThreshold;
        }

        public boolean hasLowThreshold() {
            return lowThreshold > 0;
        }

        public int getLowThreshold() {
            return lowThreshold;
        }
    }

}
