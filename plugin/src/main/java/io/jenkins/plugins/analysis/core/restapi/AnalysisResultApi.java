package io.jenkins.plugins.analysis.core.restapi;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;
import io.jenkins.plugins.util.QualityGateResult.QualityGateResultApi;

/**
 * Remote API for the {@link StaticAnalysisRun}. Simple Java Bean that exposes several methods of an {@link
 * StaticAnalysisRun} instance.
 *
 * @author Ullrich Hafner
 */
@ExportedBean
public class AnalysisResultApi {
    private final StaticAnalysisRun result;

    /**
     * Creates a new {@link AnalysisResultApi}.
     *
     * @param result
     *         the result to expose the properties from
     */
    public AnalysisResultApi(final StaticAnalysisRun result) {
        this.result = result;
    }

    @Exported
    public Run<?, ?> getOwner() {
        return result.getOwner();
    }

    @Exported
    public List<String> getErrorMessages() {
        return result.getErrorMessages().castToList();
    }

    @Exported
    public List<String> getInfoMessages() {
        return result.getInfoMessages().castToList();
    }

    @Exported
    public int getNoIssuesSinceBuild() {
        return result.getNoIssuesSinceBuild();
    }

    @Exported
    public int getSuccessfulSinceBuild() {
        return result.getSuccessfulSinceBuild();
    }

    @Exported(inline = true)
    public QualityGateResultApi getQualityGates() {
        return new QualityGateResultApi(result.getQualityGateResult());
    }

    @Exported
    @CheckForNull
    public Run<?, ?> getReferenceBuild() {
        return result.getReferenceBuild().orElse(null);
    }

    @Exported
    public int getTotalSize() {
        return result.getTotalSize();
    }

    @Exported
    public int getNewSize() {
        return result.getNewSize();
    }

    @Exported
    public int getFixedSize() {
        return result.getFixedSize();
    }
}
