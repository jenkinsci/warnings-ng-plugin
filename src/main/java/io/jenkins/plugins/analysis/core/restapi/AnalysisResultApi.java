package io.jenkins.plugins.analysis.core.restapi;

import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.quality.QualityGateStatus;

import hudson.model.Run;

/**
 * Remote API for the {@link AnalysisResult}. Simple Java Bean that exposes several methods of an {@link AnalysisResult}
 * instance.
 *
 * @author Ullrich Hafner
 */
@ExportedBean
public class AnalysisResultApi {
    private final AnalysisResult result;

    /**
     * Creates a new {@link AnalysisResultApi}.
     *
     * @param result
     *         the result to expose the properties from
     */
    public AnalysisResultApi(final AnalysisResult result) {
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

    @Exported
    public QualityGateStatus getQualityGateStatus() {
        return result.getQualityGateStatus();
    }

    @Exported @CheckForNull
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
