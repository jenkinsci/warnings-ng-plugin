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
     * Renders the default view of the Warnings tab.
     *
     * @param req the incoming Stapler request
     * @param rsp the HTTP response
     * @throws IOException if an I/O error occurs during forwarding
     * @throws ServletException if forwarding fails
     */
    public void doIndex(final StaplerRequest2 req, final StaplerResponse2 rsp) throws IOException, ServletException {
        var warningActions = getWarningActions();

        if (warningActions.isEmpty()) {
            throw new NoSuchElementException("No warnings found for " + getObject());
        }

        req.setAttribute("resultAction", warningActions.get(0));
        req.getView(this, "index.jelly").forward(req, rsp);
    }

    /**
     * Renders a dynamic subpage of the Warnings tab.
     *
     * <p>
     * This method is bound to the remainder of the URL path after the tab's root.
     * It looks up the {@link ResultAction} that matches the requested URL segment
     * and, if found, forwards the request to {@code index.jelly} with that action
     * as the backing model.
     *
     * @param req the incoming Stapler request
     * @param rsp the HTTP response
     * @throws IOException if an I/O error occurs during forwarding
     * @throws ServletException if forwarding fails
     */
    public void doDynamic(final StaplerRequest2 req, final StaplerResponse2 rsp) throws IOException, ServletException {
        String action = req.getRestOfPath().substring(1);

        for (ResultAction ui : getWarningActions()) {
            String urlName = ui.getUrlName();
            if (action.equals(urlName)) {
                req.setAttribute("resultAction", action);
                req.getView(this, "index.jelly").forward(req, rsp);
                return;
            }
        }

        throw new NoSuchElementException("No warnings found for " + getObject());
    }
}
