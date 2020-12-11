package io.jenkins.plugins.analysis.core.steps;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.analysis.core.util.ModelValidation;

/**
 * Descriptor base class for all analysis steps. Provides generic validation methods,
 * and list box models for UI select elements.
 *
 * @author Ullrich Hafner
 */
public abstract class AnalysisStepDescriptor extends StepDescriptor {
    private final ModelValidation model = new ModelValidation();

    /**
     * Returns a model with all available charsets.
     *
     * @return a model with all available charsets
     */
    @POST
    public ComboBoxModel doFillSourceCodeEncodingItems() {
        return model.getAllCharsets();
    }

    /**
     * Performs on-the-fly validation of the character encoding.
     *
     * @param reportEncoding
     *         the character encoding
     *
     * @return the validation result
     */
    @POST
    public FormValidation doCheckReportEncoding(@QueryParameter final String reportEncoding) {
        return model.validateCharset(reportEncoding);
    }

    /**
     * Performs on-the-fly validation on the character encoding.
     *
     * @param sourceCodeEncoding
     *         the character encoding
     *
     * @return the validation result
     */
    @POST
    public FormValidation doCheckSourceCodeEncoding(@QueryParameter final String sourceCodeEncoding) {
        return model.validateCharset(sourceCodeEncoding);
    }

    /**
     * Returns a model with all available severity filters.
     *
     * @return a model with all available severity filters
     */
    @POST
    public ListBoxModel doFillMinimumSeverityItems() {
        return model.getAllSeverityFilters();
    }

    /**
     * Returns the model with the possible reference jobs.
     *
     * @return the model with the possible reference jobs
     * @deprecated not used anymore, part of forensics plugin
     */
    @Deprecated @POST
    public ComboBoxModel doFillReferenceJobNameItems() {
        return model.getAllJobs();
    }

    /**
     * Performs on-the-fly validation of the health report thresholds.
     *
     * @param healthy
     *         the healthy threshold
     * @param unhealthy
     *         the unhealthy threshold
     *
     * @return the validation result
     */
    public FormValidation doCheckHealthy(@QueryParameter final int healthy, @QueryParameter final int unhealthy) {
        return model.validateHealthy(healthy, unhealthy);
    }

    /**
     * Performs on-the-fly validation of the health report thresholds.
     *
     * @param healthy
     *         the healthy threshold
     * @param unhealthy
     *         the unhealthy threshold
     *
     * @return the validation result
     */
    public FormValidation doCheckUnhealthy(@QueryParameter final int healthy, @QueryParameter final int unhealthy) {
        return model.validateUnhealthy(healthy, unhealthy);
    }

    /**
     * Returns a model with all aggregation trend chart positions.
     *
     * @return a model with all  aggregation trend chart positions
     */
    @POST
    public ListBoxModel doFillTrendChartTypeItems() {
        return model.getAllTrendChartTypes();
    }

    /**
     * Performs on-the-fly validation of the ID.
     *
     * @param id
     *         the ID of the tool
     *
     * @return the validation result
     */
    @POST
    public FormValidation doCheckId(@QueryParameter final String id) {
        return model.validateId(id);
    }
}
