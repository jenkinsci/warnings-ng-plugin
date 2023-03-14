package io.jenkins.plugins.analysis.core.steps;

import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.analysis.core.util.ModelValidation;
import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.ValidationUtilities;

/**
 * Descriptor base class for all analysis steps. Provides generic validation methods, and list box models for UI select
 * elements.
 *
 * @author Ullrich Hafner
 */
public abstract class AnalysisStepDescriptor extends StepDescriptor {
    private static final ValidationUtilities VALIDATION_UTILITIES = new ValidationUtilities();
    private static final JenkinsFacade JENKINS = new JenkinsFacade();
    private final ModelValidation model = new ModelValidation();

    /**
     * Returns a model with all available charsets.
     *
     * @param project
     *         the project that is configured
     * @return a model with all available charsets
     */
    @POST
    public ComboBoxModel doFillSourceCodeEncodingItems(@AncestorInPath final BuildableItem project) {
        if (JENKINS.hasPermission(Item.READ, project)) {
            return VALIDATION_UTILITIES.getAllCharsets();
        }
        return new ComboBoxModel();
    }

    /**
     * Performs on-the-fly validation of the character encoding.
     *
     * @param project
     *         the project that is configured
     * @param reportEncoding
     *         the character encoding
     *
     * @return the validation result
     */
    @POST
    public FormValidation doCheckReportEncoding(@AncestorInPath final BuildableItem project,
            @QueryParameter final String reportEncoding) {
        if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
            return FormValidation.ok();
        }

        return VALIDATION_UTILITIES.validateCharset(reportEncoding);
    }

    /**
     * Performs on-the-fly validation on the character encoding.
     *
     * @param project
     *         the project that is configured
     * @param sourceCodeEncoding
     *         the character encoding
     *
     * @return the validation result
     */
    @POST
    public FormValidation doCheckSourceCodeEncoding(@AncestorInPath final BuildableItem project,
            @QueryParameter final String sourceCodeEncoding) {
        if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
            return FormValidation.ok();
        }

        return VALIDATION_UTILITIES.validateCharset(sourceCodeEncoding);
    }

    /**
     * Returns a model with all available severity filters.
     *
     * @param project
     *         the project that is configured
     * @return a model with all available severity filters
     */
    @POST
    public ListBoxModel doFillMinimumSeverityItems(@AncestorInPath final BuildableItem project) {
        if (JENKINS.hasPermission(Item.READ, project)) {
            return model.getAllSeverityFilters();
        }
        return new ListBoxModel();

    }

    /**
     * Returns the model with the possible reference jobs.
     *
     * @param project
     *         the project that is configured
     * @return the model with the possible reference jobs
     * @deprecated not used anymore, part of forensics plugin
     */
    @Deprecated
    @POST
    public ComboBoxModel doFillReferenceJobNameItems(@AncestorInPath final BuildableItem project) {
        if (JENKINS.hasPermission(Item.CONFIGURE, project)) {
            return model.getAllJobs();
        }
        return new ComboBoxModel();
    }

    /**
     * Performs on-the-fly validation of the health report thresholds.
     *
     * @param project
     *         the project that is configured
     * @param healthy
     *         the healthy threshold
     * @param unhealthy
     *         the unhealthy threshold
     *
     * @return the validation result
     */
    @POST
    public FormValidation doCheckHealthy(@AncestorInPath final BuildableItem project,
            @QueryParameter final int healthy, @QueryParameter final int unhealthy) {
        if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
            return FormValidation.ok();
        }
        return model.validateHealthy(healthy, unhealthy);
    }

    /**
     * Performs on-the-fly validation of the health report thresholds.
     *
     * @param project
     *         the project that is configured
     * @param healthy
     *         the healthy threshold
     * @param unhealthy
     *         the unhealthy threshold
     *
     * @return the validation result
     */
    @POST
    public FormValidation doCheckUnhealthy(@AncestorInPath final BuildableItem project,
            @QueryParameter final int healthy, @QueryParameter final int unhealthy) {
        if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
            return FormValidation.ok();
        }
        return model.validateUnhealthy(healthy, unhealthy);
    }

    /**
     * Returns a model with all aggregation trend chart positions.
     *
     * @param project
     *         the project that is configured
     * @return a model with all  aggregation trend chart positions
     */
    @POST
    public ListBoxModel doFillTrendChartTypeItems(@AncestorInPath final BuildableItem project) {
        if (JENKINS.hasPermission(Item.READ, project)) {
            return model.getAllTrendChartTypes();
        }
        return new ListBoxModel();
    }

    /**
     * Performs on-the-fly validation of the ID.
     *
     * @param project
     *         the project that is configured
     * @param id
     *         the ID of the tool
     *
     * @return the validation result
     */
    @POST
    public FormValidation doCheckId(@AncestorInPath final BuildableItem project,
            @QueryParameter final String id) {
        if (!JENKINS.hasPermission(Item.CONFIGURE, project)) {
            return FormValidation.ok();
        }

        return VALIDATION_UTILITIES.validateId(id);
    }

    @Override
    public String argumentsToString(@NonNull final Map<String, Object> namedArgs) {
        String formatted = super.argumentsToString(namedArgs);
        if (formatted != null) {
            return formatted;
        }
        return namedArgs.toString();
    }
}
