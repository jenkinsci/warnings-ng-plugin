package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Severity;
import hudson.model.Actionable;
import hudson.model.ModelObject;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import java.util.Arrays;
import java.util.List;
import org.jvnet.localizer.ResourceBundleHolder;

/**
 * Server side model that provides the data for the "Help" page of the Warnings tab of a run. The page answers the
 * question: "What do the terms on the other pages of the tab mean?". It explains the baselines (new, outstanding, and
 * fixed issues), the severities, the types and criticalities of the quality gates, and the selection of the
 * reference build. The tables of the page are created from the model (e.g., from {@link QualityGateType}), so they
 * always contain all values that can be selected in the configuration. The layout of the associated view is defined
 * in the corresponding jelly view 'index.jelly'.
 *
 * @author Ullrich Hafner
 */
public class HelpModel implements ModelObject {
    static final String URL = "help";

    private static final ResourceBundleHolder MESSAGES = ResourceBundleHolder.get(Messages.class);

    /** The IDs of the baselines, the same IDs as in the URLs of the issues page and the remote API. */
    private static final List<String> BASELINES = List.of("all", "new", "outstanding", "fixed");

    private final Actionable owner;

    /**
     * Creates a new instance of {@link HelpModel}.
     *
     * @param owner
     *         the run that owns the results
     */
    HelpModel(final Actionable owner) {
        this.owner = owner;
    }

    @Override
    public String getDisplayName() {
        return Messages.HelpModel_DisplayName();
    }

    /**
     * Returns the URL of this page, relative to the Warnings tab.
     *
     * @return the URL name
     */
    public String getUrlName() {
        return URL;
    }

    /**
     * Returns the run that owns the results. Required by {@code l:run-subpage}.
     *
     * @return the run
     */
    public Actionable getObject() {
        return owner;
    }

    /**
     * Returns the Warnings tab of the run. Used to render the side panel.
     *
     * @return the tab
     */
    public RunTab getTab() {
        return new RunTab(owner);
    }

    /**
     * Returns the baselines that classify the issues of a build compared to the reference build.
     *
     * @return the baselines
     */
    public List<HelpEntry> getBaselines() {
        return BASELINES.stream()
                .map(id -> new HelpEntry(
                        MESSAGES.format("HelpModel.Baseline." + id + ".Name"),
                        MESSAGES.format("HelpModel.Baseline." + id + ".Description")))
                .toList();
    }

    /**
     * Returns the predefined severities of the issues, ordered from the highest to the lowest severity.
     *
     * @return the severities
     */
    public List<HelpEntry> getSeverities() {
        return Severity.getPredefinedValues().stream()
                .map(severity -> new HelpEntry(
                        LocalizedSeverity.getLongLocalizedString(severity),
                        MESSAGES.format("HelpModel.Severity." + severity.getName())))
                .toList();
    }

    /**
     * Returns all types of quality gates.
     *
     * @return the types of the quality gates
     */
    public List<HelpEntry> getQualityGateTypes() {
        return Arrays.stream(QualityGateType.values())
                .map(type ->
                        new HelpEntry(type.getDisplayName(), MESSAGES.format("HelpModel.QualityGate." + type.name())))
                .toList();
    }

    /**
     * Returns all criticalities of quality gates, i.e., what happens with the step and the build if a quality gate
     * is missed. The name of a criticality is the value that is used in the configuration of a pipeline.
     *
     * @return the criticalities of the quality gates
     */
    public List<HelpEntry> getCriticalities() {
        return Arrays.stream(QualityGateCriticality.values())
                .map(criticality -> new HelpEntry(
                        criticality.name(),
                        OverviewModel.describeCriticality(criticality),
                        criticality.getStatus().getIconClass()))
                .toList();
    }

    /**
     * An entry of a table on the help page: the name of a term and its description.
     */
    public static final class HelpEntry {
        private final String name;
        private final String description;
        private final String icon;

        private HelpEntry(final String name, final String description) {
            this(name, description, "");
        }

        private HelpEntry(final String name, final String description, final String icon) {
            this.name = name;
            this.description = description;
            this.icon = icon;
        }

        /**
         * Returns the name of the term.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the description of the term.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns the icon of the term.
         *
         * @return the icon, or an empty string if the term has no icon
         */
        public String getIcon() {
            return icon;
        }
    }
}
