package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.BuildableItem;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;

import io.jenkins.plugins.util.JenkinsFacade;

/**
 * UI proxy to let users select a static analysis report based on the UI.
 *
 * @author Ullrich Hafner
 */
public class ToolSelection extends AbstractDescribableImpl<ToolSelection> {
    private String id = StringUtils.EMPTY;

    /** Creates a new instance of {@link ToolSelection}. */
    @DataBoundConstructor
    public ToolSelection() {
        super();
        // empty constructor required for stapler
    }

    /**
     * Selects the ID of the static analysis results.
     *
     * @param id
     *         the ID of the static analysis results
     */
    @DataBoundSetter
    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public ToolSelectionDescriptor getDescriptor() {
        return (ToolSelectionDescriptor) super.getDescriptor();
    }

    /**
     * Creates a filter that can be used to filter {@link ResultAction} instances for a given set
     * of tools.
     *
     * @param canSelectTools
     *         if {@code true} the selection of tools is done by selecting the corresponding IDs, otherwise all
     *         available tools in a job are automatically selected
     * @param selectedTools
     *         the tools that should be taken into account
     *
     * @return filter {@link Predicate}
     */
    public static Predicate<ResultAction> createToolFilter(final boolean canSelectTools,
            final List<ToolSelection> selectedTools) {
        if (canSelectTools) {
            return action -> StringUtils.containsAnyIgnoreCase(action.getId(), getIds(selectedTools));
        }
        else {
            return jobAction -> true;
        }
    }

    /**
     * Returns all IDs of the specified tools.
     *
     * @param tools
     *         the tools
     *
     * @return the IDs of the tools
     */
    public static String[] getIds(final List<ToolSelection> tools) {
        return tools.stream().map(ToolSelection::getId).toArray(String[]::new);
    }

    /** Descriptor for {@link ToolSelection}. **/
    @Extension
    public static class ToolSelectionDescriptor extends Descriptor<ToolSelection> {
        // empty constructor required for stapler

        private static JenkinsFacade jenkinsFacade = new JenkinsFacade();

        @VisibleForTesting
        static void setJenkinsFacade(final JenkinsFacade facade) {
            jenkinsFacade = facade;
        }

        /**
         * Returns a model that contains all static analysis tool IDs of all jobs.
         *
         * @param project
         *         the project that is configured
         * @return a model with all static analysis tool IDs of all jobs
         */
        @POST
        public ComboBoxModel doFillIdItems(@AncestorInPath final BuildableItem project) {
            var model = new ComboBoxModel();
            if (jenkinsFacade.hasPermission(Item.CONFIGURE, project)) {
                model.addAll(collectAvailableIds());
            }
            return model;
        }

        private Set<String> collectAvailableIds() {
            return jenkinsFacade.getAllJobs()
                    .stream()
                    .flatMap(job -> job.getActions(JobAction.class).stream())
                    .map(JobAction::getId).collect(Collectors.toSet());
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
            if (!new JenkinsFacade().hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }
            if (collectAvailableIds().contains(id)) {
                return FormValidation.ok();
            }
            return FormValidation.error("None of the selected jobs contains the tool " + id);
        }
    }
}
