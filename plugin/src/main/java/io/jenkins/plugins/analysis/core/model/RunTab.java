package io.jenkins.plugins.analysis.core.model;

import hudson.model.Actionable;
import jenkins.management.Badge;
import jenkins.model.Tab;
import jenkins.model.experimentalflags.BooleanUserExperimentalFlag;
import jenkins.model.experimentalflags.NewBuildPageUserExperimentalFlag;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpRedirect;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Defines the Warnings tab for a run.
 */
public class RunTab extends Tab {
    /**
     * Constructs a new {@link RunTab}.
     * @param object the run to construct the tab for
     */
    public RunTab(final Actionable object) {
        super(object);
    }

    @Override
    public String getIconFileName() {
        if (getObject().getActions(ResultAction.class).isEmpty()) {
            return null;
        }

        return "symbol-warning-outline plugin-ionicons-api";
    }

    @Override
    public String getDisplayName() {
        return "Warnings";
    }

    @Override
    public String getUrlName() {
        return "warnings";
    }

    @Override
    public Badge getBadge() {
        var warningActionsCount = getWarningActions().stream().map(e -> e.getResult().getTotalSize()).reduce(0, Integer::sum);

        if (warningActionsCount == 0) {
            return null;
        }

        return new Badge(String.valueOf(warningActionsCount), Messages.ResultAction_Badge(warningActionsCount), Badge.Severity.WARNING);
    }

    /**
     * The list of warning actions belonging to a run.
     * @return the list of ordered warning actions, sorted by warning count, then alphabetized.
     */
    @Restricted(NoExternalUse.class)
    public List<ResultAction> getWarningActions() {
        return getObject()
                .getActions(ResultAction.class)
                .stream()
                .sorted(Comparator
                        .comparingInt((ResultAction a) -> a.getResult().getTotalSize())
                        .reversed()
                        .thenComparing(ResultAction::getDisplayName, String.CASE_INSENSITIVE_ORDER)
                )
                .toList();
    }

    /**
     * Redirects to first warning action if new UI enabled
     */
    public HttpRedirect doIndex() {
        Boolean newUiEnabled = BooleanUserExperimentalFlag.
                getFlagValueForCurrentUser("jenkins.model.experimentalflags.NewBuildPageUserExperimentalFlag");

        if (Boolean.TRUE.equals(newUiEnabled)) {
            return new HttpRedirect(getWarningActions().get(0).getUrlName());
        }

        throw new RuntimeException("This page requires the new build page UI to be enabled");
    }

    /**
     * Renders a dynamic warning action of the Warnings tab.
     */
    public ResultAction getDynamic(String name) {
        for (ResultAction ui : getWarningActions()) {
            String urlName = ui.getUrlName();
            if (name.equals(urlName)) {
                return ui;
            }
        }

        throw new NoSuchElementException("No warnings found for " + getObject());
    }
}
