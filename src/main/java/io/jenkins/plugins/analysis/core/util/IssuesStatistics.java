package io.jenkins.plugins.analysis.core.util;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;

/**
 * Simple data class that determines the number of issues (by severity, new and total and delta). This class basically
 * serves as a small proxy that provides method references to read the right property of a {@link AnalysisResult}.
 *
 * @author Ullrich Hafner
 */
public class IssuesStatistics {
    private final int totalSize;
    private final int newSize;
    private final int deltaSize;

    private final int totalErrorSize;
    private final int newErrorSize;
    private final int deltaErrorSize;

    private final int totalHighSize;
    private final int newHighSize;
    private final int deltaHighSize;

    private final int totalNormalSize;
    private final int newNormalSize;
    private final int deltaNormalSize;

    private final int totalLowSize;
    private final int newLowSize;
    private final int deltaLowSize;

    @SuppressWarnings("ParameterNumber")
    IssuesStatistics(final int totalSize, final int newSize, final int deltaSize,
            final int totalErrorSize, final int newErrorSize, final int deltaErrorSize,
            final int totalHighSize, final int newHighSize, final int deltaHighSize,
            final int totalNormalSize, final int newNormalSize, final int deltaNormalSize,
            final int totalLowSize, final int newLowSize, final int deltaLowSize) {
        this.totalSize = totalSize;
        this.newSize = newSize;
        this.deltaSize = deltaSize;
        this.totalErrorSize = totalErrorSize;
        this.newErrorSize = newErrorSize;
        this.deltaErrorSize = deltaErrorSize;
        this.totalHighSize = totalHighSize;
        this.newHighSize = newHighSize;
        this.deltaHighSize = deltaHighSize;
        this.totalNormalSize = totalNormalSize;
        this.newNormalSize = newNormalSize;
        this.deltaNormalSize = deltaNormalSize;
        this.totalLowSize = totalLowSize;
        this.newLowSize = newLowSize;
        this.deltaLowSize = deltaLowSize;
    }

    static int getTotalSize(final IssuesStatistics instance) {
        return instance.totalSize;
    }

    static int getNewSize(final IssuesStatistics instance) {
        return instance.newSize;
    }

    static int getDeltaSize(final IssuesStatistics instance) {
        return instance.deltaSize;
    }

    static int getTotalErrorSize(final IssuesStatistics instance) {
        return instance.totalErrorSize;
    }

    static int getNewErrorSize(final IssuesStatistics instance) {
        return instance.newErrorSize;
    }

    static int getDeltaErrorSize(final IssuesStatistics instance) {
        return instance.deltaErrorSize;
    }

    static int getTotalHighSize(final IssuesStatistics instance) {
        return instance.totalHighSize;
    }

    static int getNewHighSize(final IssuesStatistics instance) {
        return instance.newHighSize;
    }

    static int getDeltaHighSize(final IssuesStatistics instance) {
        return instance.deltaHighSize;
    }

    static int getTotalNormalSize(final IssuesStatistics instance) {
        return instance.totalNormalSize;
    }

    static int getNewNormalSize(final IssuesStatistics instance) {
        return instance.newNormalSize;
    }

    static int getDeltaNormalSize(final IssuesStatistics instance) {
        return instance.deltaNormalSize;
    }

    static int getTotalLowSize(final IssuesStatistics instance) {
        return instance.totalLowSize;
    }

    static int getNewLowSize(final IssuesStatistics instance) {
        return instance.newLowSize;
    }

    static int getDeltaLowSize(final IssuesStatistics instance) {
        return instance.deltaLowSize;
    }
}
