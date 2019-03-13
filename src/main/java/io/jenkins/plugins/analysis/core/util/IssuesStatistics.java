package io.jenkins.plugins.analysis.core.util;

/**
 * Simple data class that determines the number of issues (by severity, new and total and delta).
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

    @SuppressWarnings("")
    public IssuesStatistics(final int totalSize, final int newSize, final int deltaSize, final int totalErrorSize,
            final int newErrorSize,
            final int deltaErrorSize, final int totalHighSize, final int newHighSize, final int deltaHighSize,
            final int totalNormalSize,
            final int newNormalSize, final int deltaNormalSize, final int totalLowSize, final int newLowSize,
            final int deltaLowSize) {
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

    public static int getTotalSize(final IssuesStatistics instance) {
        return instance.totalSize;
    }

    public static int getNewSize(final IssuesStatistics instance) {
        return instance.newSize;
    }

    public static int getDeltaSize(final IssuesStatistics instance) {
        return instance.deltaSize;
    }

    public static int getTotalErrorSize(final IssuesStatistics instance) {
        return instance.totalErrorSize;
    }

    public static int getNewErrorSize(final IssuesStatistics instance) {
        return instance.newErrorSize;
    }

    public static int getDeltaErrorSize(final IssuesStatistics instance) {
        return instance.deltaErrorSize;
    }

    public static int getTotalHighSize(final IssuesStatistics instance) {
        return instance.totalHighSize;
    }

    public static int getNewHighSize(final IssuesStatistics instance) {
        return instance.newHighSize;
    }

    public static int getDeltaHighSize(final IssuesStatistics instance) {
        return instance.deltaHighSize;
    }

    public static int getTotalNormalSize(final IssuesStatistics instance) {
        return instance.totalNormalSize;
    }

    public static int getNewNormalSize(final IssuesStatistics instance) {
        return instance.newNormalSize;
    }

    public static int getDeltaNormalSize(final IssuesStatistics instance) {
        return instance.deltaNormalSize;
    }

    public static int getTotalLowSize(final IssuesStatistics instance) {
        return instance.totalLowSize;
    }

    public static int getNewLowSize(final IssuesStatistics instance) {
        return instance.newLowSize;
    }

    public static int getDeltaLowSize(final IssuesStatistics instance) {
        return instance.deltaLowSize;
    }
}
