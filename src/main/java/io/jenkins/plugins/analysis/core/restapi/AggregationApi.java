package io.jenkins.plugins.analysis.core.restapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.views.ResultAction;

/**
 * Remote API to list all available analysis results.
 *
 * @author Ullrich Hafner
 */
@ExportedBean
public class AggregationApi {
    private final Collection<ResultAction> results;

    /**
     * Creates a new {@link AggregationApi}.
     *
     * @param results
     *         the results to collect
     */
    public AggregationApi(final Collection<ResultAction> results) {
        this.results = new ArrayList<>(results);
    }

    @Exported(inline = true)
    public List<ToolApi> getTools() {
        return results.stream().map(this::createToolApi).collect(Collectors.toList());
    }

    @SuppressWarnings("deprecation") // this is the only way for remote API calls to obtain the absolute path
    private ToolApi createToolApi(final ResultAction result) {
        return new ToolApi(result.getId(), result.getDisplayName(),
                result.getOwner().getAbsoluteUrl() + result.getUrlName(), result.getResult().getTotalSize());
    }
}
