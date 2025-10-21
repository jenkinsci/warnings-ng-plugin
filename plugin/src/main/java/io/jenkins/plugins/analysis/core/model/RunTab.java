package io.jenkins.plugins.analysis.core.model;

import hudson.model.Actionable;
import jenkins.model.Tab;

import java.util.List;

/**
 *
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

    public List<ResultAction> getWarningActions() {
        return getObject().getActions(ResultAction.class);
    }
}
