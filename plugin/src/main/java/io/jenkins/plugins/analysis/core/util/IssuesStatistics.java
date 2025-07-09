package io.jenkins.plugins.analysis.core.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.impl.factory.Maps;

import edu.hm.hafner.analysis.Severity;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jvnet.localizer.Localizable;

/**
 * Simple data class that determines the total number of issues (by severity, new, total, fixed and delta) in a build.
 *
 * @author Ullrich Hafner
 */
public class IssuesStatistics implements Serializable {
    @Serial
    private static final long serialVersionUID = 2885481384170602793L;

    private final int totalErrorSize;
    private final int totalHighSize;
    private final int totalNormalSize;
    private final int totalLowSize;
    private final int totalModifiedSize; // @since 11.0.0

    private final int newErrorSize;
    private final int newHighSize;
    private final int newNormalSize;
    private final int newLowSize;
    private final int newModifiedSize; // @since 11.0.0

    private final int deltaErrorSize;
    private final int deltaHighSize;
    private final int deltaNormalSize;
    private final int deltaLowSize;

    private final int fixedSize;

    private final Map<Severity, Integer> totalSizeBySeverity = new HashMap<>();
    private final Map<Severity, Integer> newSizeBySeverity = new HashMap<>();

    @SuppressWarnings("checkstyle:ParameterNumber")
    IssuesStatistics(
            final int totalErrorSize, final int totalHighSize, final int totalNormalSize, final int totalLowSize,
            final int totalModifiedSize,
            final int newErrorSize, final int newHighSize, final int newNormalSize, final int newLowSize,
            final int newModifiedSize,
            final int deltaErrorSize, final int deltaHighSize, final int deltaNormalSize, final int deltaLowSize,
            final int fixedSize) {
        this.totalErrorSize = totalErrorSize;
        this.totalHighSize = totalHighSize;
        this.totalNormalSize = totalNormalSize;
        this.totalLowSize = totalLowSize;
        this.totalModifiedSize = totalModifiedSize;

        totalSizeBySeverity.put(Severity.ERROR, totalErrorSize);
        totalSizeBySeverity.put(Severity.WARNING_HIGH, totalHighSize);
        totalSizeBySeverity.put(Severity.WARNING_NORMAL, totalNormalSize);
        totalSizeBySeverity.put(Severity.WARNING_LOW, totalLowSize);

        this.newErrorSize = newErrorSize;
        this.newHighSize = newHighSize;
        this.newNormalSize = newNormalSize;
        this.newLowSize = newLowSize;
        this.newModifiedSize = newModifiedSize;

        newSizeBySeverity.put(Severity.ERROR, newErrorSize);
        newSizeBySeverity.put(Severity.WARNING_HIGH, newHighSize);
        newSizeBySeverity.put(Severity.WARNING_NORMAL, newNormalSize);
        newSizeBySeverity.put(Severity.WARNING_LOW, newLowSize);

        this.deltaErrorSize = deltaErrorSize;
        this.deltaHighSize = deltaHighSize;
        this.deltaNormalSize = deltaNormalSize;
        this.deltaLowSize = deltaLowSize;

        this.fixedSize = fixedSize;
    }

    /**
     * Aggregates the specified statistics with this statistics to a new instance, that contains all totals summed up.
     *
     * @param other the other statistics to aggregate
     * @return the aggregated statistics
     */
    public IssuesStatistics aggregate(final IssuesStatistics other) {
        return new IssuesStatistics(
                totalErrorSize + other.totalErrorSize,
                totalHighSize + other.totalHighSize,
                totalNormalSize + other.totalNormalSize,
                totalLowSize + other.totalLowSize,
                totalModifiedSize + other.totalModifiedSize,
                newErrorSize + other.newErrorSize,
                newHighSize + other.newHighSize,
                newNormalSize + other.newNormalSize,
                newLowSize + other.newLowSize,
                newModifiedSize + other.newModifiedSize,
                deltaErrorSize + other.deltaErrorSize,
                deltaHighSize + other.deltaHighSize,
                deltaNormalSize + other.deltaNormalSize,
                deltaLowSize + other.deltaLowSize,
                fixedSize + other.fixedSize);
    }

    @Whitelisted
    public int getTotalSize() {
        return totalErrorSize + totalHighSize + totalNormalSize + totalLowSize;
    }

    @Whitelisted
    public int getTotalModifiedSize() {
        return totalModifiedSize;
    }

    @Whitelisted
    public int getNewSize() {
        return newErrorSize + newHighSize + newNormalSize + newLowSize;
    }

    @Whitelisted
    public int getNewModifiedSize() {
        return newModifiedSize;
    }

    @Whitelisted
    public int getDeltaSize() {
        return deltaErrorSize + deltaHighSize + deltaNormalSize + deltaLowSize;
    }

    @Whitelisted
    public int getTotalErrorSize() {
        return totalErrorSize;
    }

    @Whitelisted
    public int getTotalHighSize() {
        return totalHighSize;
    }

    @Whitelisted
    public int getTotalNormalSize() {
        return totalNormalSize;
    }

    @Whitelisted
    public int getTotalLowSize() {
        return totalLowSize;
    }

    @Whitelisted
    public int getNewErrorSize() {
        return newErrorSize;
    }

    @Whitelisted
    public int getNewHighSize() {
        return newHighSize;
    }

    @Whitelisted
    public int getNewNormalSize() {
        return newNormalSize;
    }

    @Whitelisted
    public int getNewLowSize() {
        return newLowSize;
    }

    @Whitelisted
    public int getDeltaErrorSize() {
        return deltaErrorSize;
    }

    @Whitelisted
    public int getDeltaHighSize() {
        return deltaHighSize;
    }

    @Whitelisted
    public int getDeltaNormalSize() {
        return deltaNormalSize;
    }

    @Whitelisted
    public int getDeltaLowSize() {
        return deltaLowSize;
    }

    @Whitelisted
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
        var that = (IssuesStatistics) o;
        return totalErrorSize == that.totalErrorSize
                && totalHighSize == that.totalHighSize
                && totalNormalSize == that.totalNormalSize
                && totalLowSize == that.totalLowSize
                && totalModifiedSize == that.totalModifiedSize
                && newErrorSize == that.newErrorSize
                && newHighSize == that.newHighSize
                && newNormalSize == that.newNormalSize
                && newLowSize == that.newLowSize
                && newModifiedSize == that.newModifiedSize
                && deltaErrorSize == that.deltaErrorSize
                && deltaHighSize == that.deltaHighSize
                && deltaNormalSize == that.deltaNormalSize
                && deltaLowSize == that.deltaLowSize
                && fixedSize == that.fixedSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                totalErrorSize, totalHighSize, totalNormalSize, totalLowSize, totalModifiedSize,
                newErrorSize, newHighSize, newNormalSize, newLowSize, totalModifiedSize,
                deltaErrorSize, deltaHighSize, deltaNormalSize, deltaLowSize, fixedSize);
    }

    /**
     * Available report statistics.
     */
    public enum StatisticProperties {
        TOTAL(Messages._Statistics_Total(), IssuesStatistics::getTotalSize, StringUtils.EMPTY),
        TOTAL_ERROR(Messages._Statistics_Total_Error(), IssuesStatistics::getTotalErrorSize, "error"),
        TOTAL_HIGH(Messages._Statistics_Total_High(), IssuesStatistics::getTotalHighSize, "high"),
        TOTAL_NORMAL(Messages._Statistics_Total_Normal(), IssuesStatistics::getTotalNormalSize, "normal"),
        TOTAL_LOW(Messages._Statistics_Total_Low(), IssuesStatistics::getTotalLowSize, "low"),
        TOTAL_MODIFIED(Messages._Statistics_Total_Modified(), IssuesStatistics::getTotalModifiedSize, "modified"),

        NEW(Messages._Statistics_New(), IssuesStatistics::getNewSize, "new"),
        NEW_ERROR(Messages._Statistics_New_Error(), IssuesStatistics::getNewErrorSize, "new/error"),
        NEW_HIGH(Messages._Statistics_New_High(), IssuesStatistics::getNewHighSize, "new/high"),
        NEW_NORMAL(Messages._Statistics_New_Normal(), IssuesStatistics::getNewNormalSize, "new/normal"),
        NEW_LOW(Messages._Statistics_New_Low(), IssuesStatistics::getNewLowSize, "new/low"),
        NEW_MODIFIED(Messages._Statistics_New_Modified(), IssuesStatistics::getNewModifiedSize, "new/modified"),

        DELTA(Messages._Statistics_Delta(), IssuesStatistics::getDeltaSize, StringUtils.EMPTY),
        DELTA_ERROR(Messages._Statistics_Delta_Error(), IssuesStatistics::getDeltaErrorSize, StringUtils.EMPTY),
        DELTA_HIGH(Messages._Statistics_Delta_High(), IssuesStatistics::getDeltaHighSize, StringUtils.EMPTY),
        DELTA_NORMAL(Messages._Statistics_Delta_Normal(), IssuesStatistics::getDeltaNormalSize, StringUtils.EMPTY),
        DELTA_LOW(Messages._Statistics_Delta_Low(), IssuesStatistics::getDeltaLowSize, StringUtils.EMPTY),

        FIXED(Messages._Statistics_Fixed(), IssuesStatistics::getFixedSize, "fixed");

        private final Localizable displayName;
        private final SerializableGetter sizeGetter;
        private final String url;

        StatisticProperties(final Localizable displayName, final SerializableGetter sizeGetter,
                final String url) {
            this.displayName = displayName;
            this.sizeGetter = sizeGetter;
            this.url = url;
        }

        /**
         * Returns the localized human-readable name of this instance.
         *
         * @return human-readable name
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
         * Returns the relative url of this statistics.
         *
         * @param prefix
         *         the prefix added to the url
         *
         * @return the relative url
         */
        public String getUrl(final String prefix) {
            if (StringUtils.isEmpty(url)) {
                return prefix;
            }
            return prefix + "/" + url;
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

        /**
         * Make sure that the method reference is serializable.
         */
        @FunctionalInterface
        private interface SerializableGetter extends Function<IssuesStatistics, Integer>, Serializable {
            // nothing to add
        }
    }
}
