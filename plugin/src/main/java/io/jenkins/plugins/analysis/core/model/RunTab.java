package io.jenkins.plugins.analysis.core.model;

import hudson.model.Actionable;
import jakarta.servlet.ServletException;
import jenkins.management.Badge;
import jenkins.model.Tab;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * TODO
 */
public class RunTab extends Tab {

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

        return new Badge(String.valueOf(warningActionsCount), warningActionsCount + " warnings", Badge.Severity.WARNING);
    }

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

    public void doIndex(final StaplerRequest2 req, final StaplerResponse2 rsp) throws IOException, ServletException {
        req.setAttribute("resultAction", getWarningActions().get(0));
        req.getView(this, "index.jelly").forward(req, rsp);
    }

    public void doDynamic(final StaplerRequest2 req, final StaplerResponse2 rsp) throws IOException, ServletException {
        String action = req.getRestOfPath().substring(1);
        ResultAction resultAction = null;

        for (ResultAction ui : getWarningActions()) {
            String urlName = ui.getUrlName();
            if (urlName != null && urlName.equals(action)) {
                resultAction = ui;
            }
        }

        if (resultAction == null) {
            return;
        }

        req.setAttribute("resultAction", resultAction);
        req.getView(this, "index.jelly").forward(req, rsp);
    }
}
