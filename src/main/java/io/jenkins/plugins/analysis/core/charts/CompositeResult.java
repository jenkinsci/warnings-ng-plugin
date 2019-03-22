package io.jenkins.plugins.analysis.core.charts;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.hm.hafner.analysis.Severity;
import edu.umd.cs.findbugs.annotations.NonNull;

import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

/**
 * Combines the history results of several {@link AnalysisBuildResult static analysis results} into a single result
 * history.
 *
 * @author Ullrich Hafner
 */
public class CompositeResult implements Iterable<AnalysisBuildResult> {
    private final Collection<AnalysisBuildResult> results;

    /**
     * Creates a new instance of {@link CompositeResult}.
     *
     * @param historyOfTools
     *         the history of results for each tool
     */
    public CompositeResult(final List<Iterable<? extends AnalysisBuildResult>> historyOfTools) {
        SortedMap<AnalysisBuild, AnalysisBuildResult> resultsByBuild = new TreeMap<>();
        for (Iterable<? extends AnalysisBuildResult> toolHistory : historyOfTools) {
            for (AnalysisBuildResult toolResult : toolHistory) {
                resultsByBuild.merge(toolResult.getBuild(), toolResult, CompositeAnalysisBuildResult::new);
            }
        }
        results = resultsByBuild.values();
    }

    @NonNull
    @Override
    public Iterator<AnalysisBuildResult> iterator() {
        return results.iterator();
    }

    /**
     * Merges two {@link AnalysisBuildResult tool results} into a combined result.
     */
    static class CompositeAnalysisBuildResult implements AnalysisBuildResult {
        private final AnalysisBuildResult first;
        private final AnalysisBuildResult second;

        CompositeAnalysisBuildResult(final AnalysisBuildResult first, final AnalysisBuildResult second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public Map<String, Integer> getSizePerOrigin() {
            Map<String, Integer> sizes = new HashMap<>(first.getSizePerOrigin());
            sizes.putAll(second.getSizePerOrigin());
            return sizes;
        }

        @Override
        public AnalysisBuild getBuild() {
            return first.getBuild();
        }

        @Override
        public int getFixedSize() {
            return first.getFixedSize() + second.getFixedSize();
        }

        @Override
        public int getTotalSize() {
            return first.getTotalSize() + second.getTotalSize();
        }

        @Override
        public int getTotalSizeOf(final Severity severity) {
            return first.getTotalSizeOf(severity) + second.getTotalSizeOf(severity);
        }

        @Override
        public int getNewSize() {
            return first.getNewSize() + second.getNewSize();
        }

        @Override
        public int getNewSizeOf(final Severity severity) {
            return first.getNewSizeOf(severity) + second.getNewSizeOf(severity);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CompositeAnalysisBuildResult that = (CompositeAnalysisBuildResult) o;
            return first.equals(that.first) && second.equals(that.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}
