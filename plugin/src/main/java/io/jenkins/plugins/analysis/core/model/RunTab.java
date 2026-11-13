package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.util.VisibleForTesting;
import hudson.model.Actionable;
import hudson.model.Run;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import jenkins.management.Badge;
import jenkins.model.Tab;
import jenkins.model.experimentalflags.BooleanUserExperimentalFlag;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.verb.GET;

/**
 * Defines the Warnings tab for a run.
 */
public class RunTab extends Tab {
    static final String URL = "warnings";

    private final NewBuildPageFacade newBuildPage;

    /**
     * Constructs a new {@link RunTab}.
     * @param object the run to construct the tab for
     */
    public RunTab(final Actionable object) {
        this(object, new NewBuildPageFacade());
    }

    /**
     * Constructs a new {@link RunTab} with a custom facade for the experimental flag of the new build page.
     *
     * @param object
     *         the run to construct the tab for
     * @param newBuildPage
     *         the facade that determines whether the new build page is enabled
     */
    @VisibleForTesting
    RunTab(final Actionable object, final NewBuildPageFacade newBuildPage) {
        super(object);

        this.newBuildPage = newBuildPage;
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
        return URL;
    }

    @Override
    public Badge getBadge() {
        var warningActionsCount = getWarningActions().stream()
                .map(e -> e.getResult().getTotalSize())
                .reduce(0, Integer::sum);

        if (warningActionsCount == 0) {
            return null;
        }

        return new Badge(
                String.valueOf(warningActionsCount),
                Messages.ResultAction_Badge(warningActionsCount),
                Badge.Severity.WARNING);
    }

    /**
     * Generates the widget for the Overview tab.
     * @return the widget.
     */
    public Widget getWidget() {
        return new Widget(getWarningActions());
    }

    /**
     * The list of warning actions belonging to a run.
     * @return the list of ordered warning actions, sorted by warning count, then alphabetized.
     */
    @Restricted(NoExternalUse.class)
    public List<ResultAction> getWarningActions() {
        return getObject().getActions(ResultAction.class).stream()
                .sorted(Comparator.comparingInt(
                                (ResultAction a) -> a.getResult().getTotalSize())
                        .reversed()
                        .thenComparing(ResultAction::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Returns the model of the overview page that summarizes the results of all warning actions. Stapler resolves
     * this getter before {@link #getDynamic(String)}, so the URL {@code warnings/overview} always shows the overview.
     *
     * @return the overview model
     */
    @Restricted(NoExternalUse.class)
    public OverviewModel getOverview() {
        return new OverviewModel(getObject(), getWarningActions());
    }

    /**
     * Returns the model of the trend page that shows the trend of the results of all warning actions (or of a
     * single action). Stapler resolves this getter before {@link #getDynamic(String)}, so the URL
     * {@code warnings/trend} always shows the trend.
     *
     * @return the trend model
     */
    @Restricted(NoExternalUse.class)
    public TrendModel getTrend() {
        return new TrendModel(getObject(), getWarningActions());
    }

    /**
     * Redirects to the overview page if the new build page is enabled. The redirect target is derived from the URL of
     * the run (and not from the request), and it is resolved via the context path, so it always points to the
     * overview page of this run, independent of whether the request URL ends with a slash.
     *
     * @return the redirect to the overview page, or a 404 error if the new build page is disabled
     */
    @GET
    public HttpResponse doIndex() {
        if (newBuildPage.isEnabled() && getObject() instanceof Run<?, ?> run) {
            return HttpResponses.redirectViaContextPath(run.getUrl() + URL + "/" + OverviewModel.URL);
        }

        return HttpResponses.errorWithoutStack(
                HttpServletResponse.SC_NOT_FOUND, "This page requires the new build page UI to be enabled");
    }

    /**
     * Renders a dynamic warning action of the Warnings tab.
     * @param name the name of the warning action to render.
     * @return the warning action.
     */
    public ResultAction getDynamic(final String name) {
        for (ResultAction ui : getWarningActions()) {
            String urlName = ui.getUrlName();
            if (name.equals(urlName)) {
                return ui;
            }
        }

        throw new NoSuchElementException("No warnings found for " + getObject());
    }

    /**
     * Facade for the experimental flag of the new build page. Encapsulates the static access to the current user so
     * that the flag can be replaced in tests.
     */
    @VisibleForTesting
    static class NewBuildPageFacade {
        private static final String NEW_BUILD_PAGE_FLAG =
                "jenkins.model.experimentalflags.NewBuildPageUserExperimentalFlag";

        /**
         * Returns whether the new build page is enabled for the current user.
         *
         * @return {@code true} if the new build page is enabled, {@code false} otherwise
         */
        boolean isEnabled() {
            return Boolean.TRUE.equals(BooleanUserExperimentalFlag.getFlagValueForCurrentUser(NEW_BUILD_PAGE_FLAG));
        }
    }
}
