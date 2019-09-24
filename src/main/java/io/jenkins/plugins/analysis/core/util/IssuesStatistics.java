package io.jenkins.plugins.analysis.core.util;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.impl.factory.Maps;

import edu.hm.hafner.analysis.Severity;

import org.jvnet.localizer.Localizable;

/**
 * Simple data class that determines the total number of issues (by severity, new, total, fixed and delta) in a build.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.TooManyFields")
public class IssuesStatistics implements Serializable {
    private static final long serialVersionUID = 2885481384170602793L;

    private final int totalErrorSize;
    private final int totalHighSize;
    private final int totalNormalSize;
    private final int totalLowSize;

    private final int newErrorSize;
    private final int newHighSize;
    private final int newNormalSize;
    private final int newLowSize;

    private final int deltaErrorSize;
    private final int deltaHighSize;
    private final int deltaNormalSize;
    private final int deltaLowSize;

    private final int fixedSize;

    private final Map<Severity, Integer> totalSizeBySeverity = Maps.mutable.empty();
    private final Map<Severity, Integer> newSizeBySeverity = Maps.mutable.empty();

    @SuppressWarnings("checkstyle:ParameterNumber")
    IssuesStatistics(
            final int totalErrorSize, final int totalHighSize, final int totalNormalSize, final int totalLowSize,
            final int newErrorSize, final int newHighSize, final int newNormalSize, final int newLowSize,
            final int deltaErrorSize, final int deltaHighSize, final int deltaNormalSize, final int deltaLowSize,
            final int fixedSize) {
        this.totalErrorSize = totalErrorSize;
        totalSizeBySeverity.put(Severity.ERROR, totalErrorSize);
        this.totalHighSize = totalHighSize;
        totalSizeBySeverity.put(Severity.WARNING_HIGH, totalHighSize);
        this.totalNormalSize = totalNormalSize;
        totalSizeBySeverity.put(Severity.WARNING_NORMAL, totalNormalSize);
        this.totalLowSize = totalLowSize;
        totalSizeBySeverity.put(Severity.WARNING_LOW, totalLowSize);

        this.newErrorSize = newErrorSize;
        newSizeBySeverity.put(Severity.ERROR, newErrorSize);
        this.newHighSize = newHighSize;
        newSizeBySeverity.put(Severity.WARNING_HIGH, newHighSize);
        this.newNormalSize = newNormalSize;
        newSizeBySeverity.put(Severity.WARNING_NORMAL, newNormalSize);
        this.newLowSize = newLowSize;
        newSizeBySeverity.put(Severity.WARNING_LOW, newLowSize);

        this.deltaErrorSize = deltaErrorSize;
        this.deltaHighSize = deltaHighSize;
        this.deltaNormalSize = deltaNormalSize;
        this.deltaLowSize = deltaLowSize;

        this.fixedSize = fixedSize;
    }

    public int getTotalSize() {
        return totalErrorSize + totalHighSize + totalNormalSize + totalLowSize;
    }

    public int getNewSize() {
        return newErrorSize + newHighSize + newNormalSize + newLowSize;
    }

    public int getDeltaSize() {
        return deltaErrorSize + deltaHighSize + deltaNormalSize + deltaLowSize;
    }

    public int getTotalErrorSize() {
        return totalErrorSize;
    }

    public int getTotalHighSize() {
        return totalHighSize;
    }

    public int getTotalNormalSize() {
        return totalNormalSize;
    }

    public int getTotalLowSize() {
        return totalLowSize;
    }

    public int getNewErrorSize() {
        return newErrorSize;
    }

    public int getNewHighSize() {
        return newHighSize;
    }

    public int getNewNormalSize() {
        return newNormalSize;
    }

    public int getNewLowSize() {
        return newLowSize;
    }

    public int getDeltaErrorSize() {
        return deltaErrorSize;
    }

    public int getDeltaHighSize() {
        return deltaHighSize;
    }

    public int getDeltaNormalSize() {
        return deltaNormalSize;
    }

    public int getDeltaLowSize() {
        return deltaLowSize;
    }

    public int getFixedSize() {
        return fixedSize;
    }

    public ImmutableMap<Severity, Integer> getTotalSizePerSeverity() {
        return Maps.immutable.ofMap(totalSizeBySeverity);
    }

    /**
     * Returns the total number of issues that have the specified {@link Severity}.
     *
     * @param severity
     *         the severity of the issues to match
     *
     * @return total number of issues
     */
    public int getTotalSizeOf(final Severity severity) {
        validateSeverity(severity);

        return totalSizeBySeverity.get(severity);
    }

    public ImmutableMap<Severity, Integer> getNewSizePerSeverity() {
        return Maps.immutable.ofMap(newSizeBySeverity);
    }

    /**
     * Returns the total number of new issues that have the specified {@link Severity}.
     *
     * @param severity
     *         the severity of the issues to match
     *
     * @return total number of issues
     */
    public int getNewSizeOf(final Severity severity) {
        validateSeverity(severity);

        return newSizeBySeverity.get(severity);
    }

    private void validateSeverity(final Severity severity) {
        if (!Severity.getPredefinedValues().contains(severity)) {
            throw new NoSuchElementException("There is no such severity: " + severity);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IssuesStatistics that = (IssuesStatistics) o;
        return totalErrorSize == that.totalErrorSize
                && totalHighSize == that.totalHighSize
                && totalNormalSize == that.totalNormalSize
                && totalLowSize == that.totalLowSize
                && newErrorSize == that.newErrorSize
                && newHighSize == that.newHighSize
                && newNormalSize == that.newNormalSize
                && newLowSize == that.newLowSize
                && deltaErrorSize == that.deltaErrorSize
                && deltaHighSize == that.deltaHighSize
                && deltaNormalSize == that.deltaNormalSize
                && deltaLowSize == that.deltaLowSize
                && fixedSize == that.fixedSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalErrorSize, totalHighSize, totalNormalSize, totalLowSize, newErrorSize, newHighSize,
                newNormalSize, newLowSize, deltaErrorSize, deltaHighSize, deltaNormalSize, deltaLowSize, fixedSize);
    }

    /**
     * Available report statistics.
     */
    public enum StatisticProperties {
        TOTAL(Messages._Statistics_Total(), IssuesStatistics::getTotalSize),
        TOTAL_ERROR(Messages._Statistics_Total_Error(), IssuesStatistics::getTotalErrorSize),
        TOTAL_HIGH(Messages._Statistics_Total_High(), IssuesStatistics::getTotalHighSize),
        TOTAL_NORMAL(Messages._Statistics_Total_Normal(), IssuesStatistics::getTotalNormalSize),
        TOTAL_LOW(Messages._Statistics_Total_Low(), IssuesStatistics::getTotalLowSize),

        NEW(Messages._Statistics_New(), IssuesStatistics::getNewSize),
        NEW_ERROR(Messages._Statistics_New_Error(), IssuesStatistics::getNewErrorSize),
        NEW_HIGH(Messages._Statistics_New_High(), IssuesStatistics::getNewHighSize),
        NEW_NORMAL(Messages._Statistics_New_Normal(), IssuesStatistics::getNewNormalSize),
        NEW_LOW(Messages._Statistics_New_Low(), IssuesStatistics::getNewLowSize),

        DELTA(Messages._Statistics_Delta(), IssuesStatistics::getDeltaSize),
        DELTA_ERROR(Messages._Statistics_Delta_Error(), IssuesStatistics::getDeltaErrorSize),
        DELTA_HIGH(Messages._Statistics_Delta_High(), IssuesStatistics::getDeltaHighSize),
        DELTA_NORMAL(Messages._Statistics_Delta_Normal(), IssuesStatistics::getDeltaNormalSize),
        DELTA_LOW(Messages._Statistics_Delta_Low(), IssuesStatistics::getDeltaLowSize),

        FIXED(Messages._Statistics_Fixed(), IssuesStatistics::getFixedSize);

        private final Localizable displayName;
        private final Function<IssuesStatistics, Integer> sizeGetter;

        StatisticProperties(final Localizable displayName, final Function<IssuesStatistics, Integer> sizeGetter) {
            this.displayName = displayName;
            this.sizeGetter = sizeGetter;
        }

        /**
         * Returns the localized human readable name of this instance.
         *
         * @return human readable name
         */
        public String getDisplayName() {
            return displayName.toString();
        }

        /**
         * Returns the method that should be used to determine the selected number of issues in the build.
         *
         * @return the threshold getter
         */
        public Function<IssuesStatistics, Integer> getSizeGetter() {
            return sizeGetter;
        }

        /**
         * Returns the selected number of issues in the build.
         *
         * @param statistics
         *         the statistics to get the value from
         *
         * @return the threshold getter
         */
        public int get(final IssuesStatistics statistics) {
            return sizeGetter.apply(statistics);
        }
    }
}
