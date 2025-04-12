package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.analysis.Severity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.IssuesStatistics;
import io.jenkins.plugins.analysis.core.util.IssuesStatisticsBuilder;

/**
 * A build result that is composed of a series of other builds results simply by summing up the number of issues.
 *
 * @author Ullrich Hafner
 */
public class CompositeBuildResult implements AnalysisBuildResult {
    private final Map<String, Integer> sizesPerOrigin;
    private final IssuesStatistics totals;

    /**
     * Creates a composition of the specified results. Adds the new value of each property to the existing value of the
     * same property.
     *
     * @param results
     *         the results to add
     */
    public CompositeBuildResult(final Collection<? extends AnalysisBuildResult> results) {
        totals = results.stream()
                .map(AnalysisBuildResult::getTotals)
                .map(Objects::requireNonNull)
                .reduce(new IssuesStatisticsBuilder().build(), IssuesStatistics::aggregate);
        sizesPerOrigin = results.stream().map(AnalysisBuildResult::getSizePerOrigin)
                .reduce(new HashMap<>(), (first, second) -> {
                    second.forEach((key, value) -> first.merge(key, value, Integer::sum));
                    return first;
                });
    }

    @Override
    public Map<String, Integer> getSizePerOrigin() {
        return sizesPerOrigin;
    }

    @Override
    public int getFixedSize() {
        return getTotals().getFixedSize();
    }

    @Override
    public int getTotalSize() {
        return getTotals().getTotalSize();
    }

    @Override
    public int getTotalSizeOf(final Severity severity) {
        return getTotals().getTotalSizeOf(severity);
    }

    @Override
    public int getNewSize() {
        return getTotals().getNewSize();
    }

    @Override
    public int getNewSizeOf(final Severity severity) {
        return getTotals().getNewSizeOf(severity);
    }

    @Override
    public IssuesStatistics getTotals() {
        return totals;
    }
}
