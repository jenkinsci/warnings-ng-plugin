package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.Actionable;
import hudson.model.Run;
import io.jenkins.plugins.util.QualityGateStatus;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import jenkins.management.Badge;
import jenkins.model.Tab;
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
    static final String ICON = "symbol-warning-outline plugin-ionicons-api";
    private static final String SOURCE_LINK_PREFIX = "source.";

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

        return ICON;
    }

    @Override
    public String getDisplayName() {
        return "Warnings";
    }

    @Override
    public String getUrlName() {
        return URL;
    }

    /**
     * Returns the badge of the Warnings tab. If at least one quality gate has been missed, the badge shows the number
     * of missed quality gates (as danger if a quality gate failed the step or the build, otherwise as warning).
     * Otherwise, the badge shows the number of new warnings. If there are no new warnings, no badge is shown.
     *
     * @return the badge, or {@code null} if no badge should be shown (as required by the Jenkins API)
     */
    @Override
    @CheckForNull
    public Badge getBadge() {
        var overview = getOverview();

        int missedQualityGates = overview.getMissedQualityGatesSize();
        if (missedQualityGates > 0) {
            var status = overview.getQualityGateStatus();
            return new Badge(
                    String.valueOf(missedQualityGates),
                    Messages.RunTab_Badge_QualityGates(missedQualityGates),
                    status == QualityGateStatus.ERROR || status == QualityGateStatus.FAILED
                            ? Badge.Severity.DANGER
                            : Badge.Severity.WARNING);
        }

        int newWarnings = overview.getNewSize();
        if (newWarnings > 0) {
            return new Badge(
                    String.valueOf(newWarnings),
                    Messages.RunTab_Badge_NewWarnings(newWarnings),
                    Badge.Severity.WARNING);
        }

        return null;
    }

    /**
     * Generates the widget for the build page.
     *
     * @return the widget
     */
    public Widget getWidget() {
        return new Widget(getOverview());
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
     * Returns the model of the issues page that shows the issues of all warning actions in a table. Stapler
     * resolves this getter before {@link #getDynamic(String)}, so the URL {@code warnings/table} always shows the
     * issues.
     *
     * @return the issues model
     */
    @Restricted(NoExternalUse.class)
    public IssuesPageModel getTable() {
        return new IssuesPageModel(getObject(), getWarningActions());
    }

    /**
     * Returns the model of the hotspots page that shows the files (or other properties) with the most issues of all
     * warning actions. Stapler resolves this getter before {@link #getDynamic(String)}, so the URL
     * {@code warnings/hotspots} always shows the hotspots.
     *
     * @return the hotspots model
     */
    @Restricted(NoExternalUse.class)
    public HotspotsModel getHotspots() {
        return new HotspotsModel(getObject(), getWarningActions());
    }

    /**
     * Returns the model of the treemap page that shows the modules, packages, and files with issues of all warning
     * actions. Stapler resolves this getter before {@link #getDynamic(String)}, so the URL {@code warnings/treemap}
     * always shows the treemap.
     *
     * @return the treemap model
     */
    @Restricted(NoExternalUse.class)
    public TreeMapModel getTreemap() {
        return new TreeMapModel(getObject(), getWarningActions());
    }

    /**
     * Returns the model of the log page that shows the error and information messages of all warning actions.
     * Stapler resolves this getter before {@link #getDynamic(String)}, so the URL {@code warnings/log} always shows
     * the log messages.
     *
     * @return the log model
     */
    @Restricted(NoExternalUse.class)
    public LogModel getLog() {
        return new LogModel(getObject(), getWarningActions());
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
     * Renders the affected file of an issue. This is required for the links to the affected files on the issues page
     * (see {@link IssuesPageModel}): these links are relative to the URL of the tab and do not contain the tool that
     * reported the issue. The results of the individual tools are not part of the Warnings tab anymore: they are
     * shown on the pages of the tab, where the tool can be selected.
     *
     * @param name
     *         the link to the affected file of an issue
     *
     * @return the view that shows the affected file
     * @throws NoSuchElementException
     *         if there is no such issue
     */
    public Object getDynamic(final String name) {
        if (name.startsWith(SOURCE_LINK_PREFIX)) {
            for (ResultAction action : getWarningActions()) {
                if (hasIssue(action, name)) {
                    return action.getTarget().createDetails(name);
                }
            }
        }

        throw new NoSuchElementException("No warnings found for " + getObject());
    }

    private boolean hasIssue(final ResultAction action, final String sourceLink) {
        return action.getResult().getIssues().stream()
                .anyMatch(issue -> (SOURCE_LINK_PREFIX + issue.getId()).equals(sourceLink));
    }
}
