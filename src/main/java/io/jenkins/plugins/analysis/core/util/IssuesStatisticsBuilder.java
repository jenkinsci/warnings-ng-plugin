package io.jenkins.plugins.analysis.core.util;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;

/**
 * Builder to create instances of the data class {@link IssuesStatistics}. This builder provides methods to configure
 * new and total and delta
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("JavaDocMethod")
public class IssuesStatisticsBuilder {
    private int totalSize;
    private int newSize;
    private int deltaSize;
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

    public IssuesStatisticsBuilder setTotalSize(final int totalSize) {
        this.totalSize = totalSize;
        return this;
    }

    public IssuesStatisticsBuilder setNewSize(final int newSize) {
        this.newSize = newSize;
        return this;
    }

    public IssuesStatisticsBuilder setDeltaSize(final int deltaSize) {
        this.deltaSize = deltaSize;
        return this;
    }

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

    public IssuesStatisticsBuilder setDeltaLowSize(final int deltaLowSize) {
        this.deltaLowSize = deltaLowSize;
        return this;
    }

    public IssuesStatistics build() {
        return new IssuesStatistics(totalSize, newSize, deltaSize, totalErrorSize, newErrorSize, deltaErrorSize,
                totalHighSize, newHighSize, deltaHighSize, totalNormalSize, newNormalSize, deltaNormalSize,
                totalLowSize, newLowSize, deltaLowSize);
    }

    void clear() {
        totalSize = 0;
        newSize = 0;
        deltaSize = 0;
        totalErrorSize = 0;
        newErrorSize = 0;
        deltaErrorSize = 0;
        totalHighSize = 0;
        newHighSize = 0;
        deltaHighSize = 0;
        totalNormalSize = 0;
        newNormalSize = 0;
        deltaNormalSize = 0;
        totalLowSize = 0;
        newLowSize = 0;
        deltaLowSize = 0;
    }
}