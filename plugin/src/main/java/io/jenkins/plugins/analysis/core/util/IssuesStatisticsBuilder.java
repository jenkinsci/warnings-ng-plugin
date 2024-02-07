package io.jenkins.plugins.analysis.core.util;

/**
 * Builder to create instances of the data class {@link IssuesStatistics}. This builder provides methods to configure
 * new and total and delta
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("checkstyle:MissingJavadocMethod")
public class IssuesStatisticsBuilder {
    private int totalErrorSize;
    private int newErrorSize;
    private int deltaErrorSize;
    private int totalHighSize;
    private int newHighSize;
    private int deltaHighSize;
    private int totalNormalSize;
    private int newNormalSize;
    private int deltaNormalSize;
    private int totalLowSize;
    private int newLowSize;
    private int deltaLowSize;
    private int totalModifiedSize; // @since 11.0.0
    private int newModifiedSize; // @since 11.0.0
    private int fixedSize;

    public IssuesStatisticsBuilder setTotalErrorSize(final int totalErrorSize) {
        this.totalErrorSize = totalErrorSize;
        return this;
    }

    public IssuesStatisticsBuilder setNewErrorSize(final int newErrorSize) {
        this.newErrorSize = newErrorSize;
        return this;
    }

    public IssuesStatisticsBuilder setDeltaErrorSize(final int deltaErrorSize) {
        this.deltaErrorSize = deltaErrorSize;
        return this;
    }

    public IssuesStatisticsBuilder setTotalHighSize(final int totalHighSize) {
        this.totalHighSize = totalHighSize;
        return this;
    }

    public IssuesStatisticsBuilder setNewHighSize(final int newHighSize) {
        this.newHighSize = newHighSize;
        return this;
    }

    public IssuesStatisticsBuilder setDeltaHighSize(final int deltaHighSize) {
        this.deltaHighSize = deltaHighSize;
        return this;
    }

    public IssuesStatisticsBuilder setTotalNormalSize(final int totalNormalSize) {
        this.totalNormalSize = totalNormalSize;
        return this;
    }

    public IssuesStatisticsBuilder setTotalModifiedSize(final int totalModifiedSize) {
        this.totalModifiedSize = totalModifiedSize;
        return this;
    }

    public IssuesStatisticsBuilder setNewNormalSize(final int newNormalSize) {
        this.newNormalSize = newNormalSize;
        return this;
    }

    public IssuesStatisticsBuilder setDeltaNormalSize(final int deltaNormalSize) {
        this.deltaNormalSize = deltaNormalSize;
        return this;
    }

    public IssuesStatisticsBuilder setTotalLowSize(final int totalLowSize) {
        this.totalLowSize = totalLowSize;
        return this;
    }

    public IssuesStatisticsBuilder setNewLowSize(final int newLowSize) {
        this.newLowSize = newLowSize;
        return this;
    }

    public IssuesStatisticsBuilder setNewModifiedSize(final int newModifiedSize) {
        this.newModifiedSize = newModifiedSize;
        return this;
    }

    public IssuesStatisticsBuilder setDeltaLowSize(final int deltaLowSize) {
        this.deltaLowSize = deltaLowSize;
        return this;
    }

    public IssuesStatisticsBuilder setFixedSize(final int fixedSize) {
        this.fixedSize = fixedSize;
        return this;
    }

    public IssuesStatistics build() {
        return new IssuesStatistics(
                totalErrorSize, totalHighSize, totalNormalSize, totalLowSize, totalModifiedSize,
                newErrorSize, newHighSize, newNormalSize, newLowSize, newModifiedSize,
                deltaErrorSize, deltaHighSize, deltaNormalSize, deltaLowSize,
                fixedSize);
    }

    void clear() {
        totalErrorSize = 0;
        totalHighSize = 0;
        totalNormalSize = 0;
        totalLowSize = 0;
        totalModifiedSize = 0;

        newErrorSize = 0;
        newHighSize = 0;
        newNormalSize = 0;
        newLowSize = 0;
        newModifiedSize = 0;

        deltaErrorSize = 0;
        deltaHighSize = 0;
        deltaNormalSize = 0;
        deltaLowSize = 0;

        fixedSize = 0;
    }
}
