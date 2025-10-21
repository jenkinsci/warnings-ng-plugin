package io.jenkins.plugins.analysis.core.model;

import hudson.model.Actionable;
import jenkins.model.Tab;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.List;

/**
 * TODO
 */
public class RunTab extends Tab {

    public RunTab(Actionable object) {
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

    @Restricted(NoExternalUse.class)
    public List<ResultAction> getWarningActions() {
        return getObject().getActions(ResultAction.class);
    }

    public ResultAction getDynamic(String name) {
        for (ResultAction ui : getWarningActions()) {
            String urlName = ui.getUrlName();
            if (urlName != null && urlName.equals(name)) {
                return ui;
            }
        }
        return null;
    }
}
