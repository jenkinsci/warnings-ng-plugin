package io.jenkins.plugins.analysis.core.charts;

import java.util.HashMap;
import java.util.Map;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

/**
 * A build result that is composed of a series of other builds results simply by summing up the number of issues.
 *
 * @author Ullrich Hafner
 */
// FIXME: IssueStatistics?
public class CompositeBuildResult implements AnalysisBuildResult {
    private final Map<String, Integer> sizesPerOrigin = new HashMap<>();
    private final Map<Severity, Integer> sizesPerSeverity = new HashMap<>();
    private final Map<Severity, Integer> newSizesPerSeverity = new HashMap<>();

    private int fixedSize = 0;

    /**
     * Adds the specified results to this composition. Adds the new value of each property to the existing value of the
     * same property.
     *
     * @param additionalResults
     *         the additional results to add
     *
     * @return returns this to simplify call chains
     */
    public CompositeBuildResult add(final AnalysisBuildResult... additionalResults) {
        for (AnalysisBuildResult another : additionalResults) {
            sizesPerOrigin.putAll(another.getSizePerOrigin());
            for (Severity severity : Severity.getPredefinedValues()) {
                sizesPerSeverity.merge(severity, another.getTotalSizeOf(severity), Integer::sum);
                newSizesPerSeverity.merge(severity, another.getNewSizeOf(severity), Integer::sum);
            }
            fixedSize += another.getFixedSize();
        }
        return this;
    }

    @Override
    public Map<String, Integer> getSizePerOrigin() {
        return sizesPerOrigin;
    }

    @Override
    public int getFixedSize() {
        return fixedSize;
    }

    @Override
    public int getTotalSize() {
        return sum(sizesPerSeverity);
    }

    @Override
    public int getTotalSizeOf(final Severity severity) {
        return sizesPerSeverity.getOrDefault(severity, 0);
    }

    @Override
    public int getNewSize() {
        return sum(newSizesPerSeverity);
    }

    private Integer sum(final Map<Severity, Integer> map) {
        return map.values().stream().reduce(0, Integer::sum);
    }

    @Override
    public int getNewSizeOf(final Severity severity) {
        return newSizesPerSeverity.getOrDefault(severity, 0);
    }
}
