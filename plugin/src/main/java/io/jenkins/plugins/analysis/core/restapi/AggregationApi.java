package io.jenkins.plugins.analysis.core.restapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Remote API to list all available analysis results.
 *
 * @author Ullrich Hafner
 */
@ExportedBean
public class AggregationApi {
    private final Collection<ToolApi> results;

    /**
     * Creates a new {@link AggregationApi}.
     *
     * @param results
     *         the results to collect
     */
    public AggregationApi(final List<ToolApi> results) {
        this.results = new ArrayList<>(results);
    }

    @Exported(inline = true)
    public List<ToolApi> getTools() {
        return new ArrayList<>(results);
    }
}
