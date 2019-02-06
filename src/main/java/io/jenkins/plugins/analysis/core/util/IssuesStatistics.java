package io.jenkins.plugins.analysis.core.util;

/**
 * FIXME: comment class.
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

    public int getTotalSize() {
        return totalSize;
    }

    public int getNewSize() {
        return newSize;
    }

    public int getDeltaSize() {
        return deltaSize;
    }

    public int getTotalErrorSize() {
        return totalErrorSize;
    }

    public int getNewErrorSize() {
        return newErrorSize;
    }

    public int getDeltaErrorSize() {
        return deltaErrorSize;
    }

    public int getTotalHighSize() {
        return totalHighSize;
    }

    public int getNewHighSize() {
        return newHighSize;
    }

    public int getDeltaHighSize() {
        return deltaHighSize;
    }

    public int getTotalNormalSize() {
        return totalNormalSize;
    }

    public int getNewNormalSize() {
        return newNormalSize;
    }

    public int getDeltaNormalSize() {
        return deltaNormalSize;
    }

    public int getTotalLowSize() {
        return totalLowSize;
    }

    public int getNewLowSize() {
        return newLowSize;
    }

    public int getDeltaLowSize() {
        return deltaLowSize;
    }
}
