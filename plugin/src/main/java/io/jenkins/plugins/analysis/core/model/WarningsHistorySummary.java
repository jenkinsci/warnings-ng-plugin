package io.jenkins.plugins.analysis.core.model;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Warnings summary of a single build that is shown in the build history of the job page. Contains the totals of all
 * static analysis results of the build: the total number of warnings splits into the existing and the new warnings.
 * Additionally, the number of fixed warnings is provided.
 *
 * @author Ullrich Hafner
 */
public final class WarningsHistorySummary {
    private static final WarningsHistorySummary NULL_OBJECT = new WarningsHistorySummary("", 0, 0, 0);

    private final String url;
    private final int totalSize;
    private final int newSize;
    private final int fixedSize;

    /**
     * Returns an empty summary, i.e., a summary of a build without static analysis results.
     *
     * @return an empty summary
     */
    static WarningsHistorySummary empty() {
        return NULL_OBJECT;
    }

    /**
     * Creates a summary of the results of the specified actions.
     *
     * @param actions
     *         the actions with the static analysis results
     * @param url
     *         the URL of the summary, relative to the build
     *
     * @return the summary
     */
    static WarningsHistorySummary create(final List<ResultAction> actions, final String url) {
        if (actions.isEmpty()) {
            return NULL_OBJECT;
        }
        var results = actions.stream().map(ResultAction::getResult).toList();
        return new WarningsHistorySummary(
                url,
                sum(results, AnalysisResult::getTotalSize),
                sum(results, AnalysisResult::getNewSize),
                sum(results, AnalysisResult::getFixedSize));
    }

    /**
     * Sums up the specified size of all results.
     *
     * @param results
     *         the results to sum up
     * @param size
     *         the function that returns the size of a result
     *
     * @return the sum
     */
    private static int sum(final List<AnalysisResult> results, final ToIntFunction<AnalysisResult> size) {
        return results.stream().mapToInt(size).sum();
    }

    /**
     * Creates a new summary.
     *
     * @param url
     *         the URL of the summary, relative to the build
     * @param totalSize
     *         the total number of warnings
     * @param newSize
     *         the number of new warnings
     * @param fixedSize
     *         the number of fixed warnings
     */
    private WarningsHistorySummary(final String url, final int totalSize, final int newSize, final int fixedSize) {
        this.url = url;
        this.totalSize = totalSize;
        this.newSize = newSize;
        this.fixedSize = fixedSize;
    }

    /**
     * Returns whether this summary is empty, i.e., whether the build has no static analysis results.
     *
     * @return {@code true} if there are no results to show, {@code false} otherwise
     */
    public boolean isEmpty() {
        return url.isEmpty();
    }

    /**
     * Returns the URL of the static analysis results, relative to the build.
     *
     * @return the URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the icon of the static analysis results.
     *
     * @return the icon
     */
    public String getIcon() {
        return RunTab.ICON;
    }

    /**
     * Returns the total number of warnings, i.e., the sum of the existing and the new warnings.
     *
     * @return the total number of warnings
     */
    public int getTotalSize() {
        return totalSize;
    }

    /**
     * Returns the number of existing warnings, i.e., the warnings that are part of the reference build as well.
     *
     * @return the number of existing warnings
     */
    public int getExistingSize() {
        return totalSize - newSize;
    }

    /**
     * Returns the number of new warnings, i.e., the warnings that are not part of the reference build.
     *
     * @return the number of new warnings
     */
    public int getNewSize() {
        return newSize;
    }

    /**
     * Returns the number of fixed warnings, i.e., the warnings of the reference build that are gone.
     *
     * @return the number of fixed warnings
     */
    public int getFixedSize() {
        return fixedSize;
    }
}
