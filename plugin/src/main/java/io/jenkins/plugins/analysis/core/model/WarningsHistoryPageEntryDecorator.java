package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import hudson.widgets.HistoryWidget;
import java.util.List;
import jenkins.model.HistoricalBuild;
import jenkins.widgets.HistoryPageEntryDecorator;

/**
 * Shows the existing, new, and fixed warnings of a build in the build history of the job page. The values of all
 * static analysis results of a build are summed up, so the build history shows the same numbers as the Warnings tab.
 * The values are read from the totals that are stored with the build, so rendering the history never loads the issues
 * of a build. The ordinal places the entry right of the test results (ordinal 0) and the coverage (ordinals -10 and
 * -20).
 *
 * @author Ullrich Hafner
 */
@Extension(ordinal = -30)
public class WarningsHistoryPageEntryDecorator extends HistoryPageEntryDecorator {
    private final NewBuildPageFacade newBuildPage;

    /**
     * Creates a new instance of {@link WarningsHistoryPageEntryDecorator}.
     */
    public WarningsHistoryPageEntryDecorator() {
        this(new NewBuildPageFacade());
    }

    /**
     * Creates a new instance of {@link WarningsHistoryPageEntryDecorator} with a custom facade for the experimental
     * flag of the new build page.
     *
     * @param newBuildPage
     *         the facade that determines whether the new build page is enabled
     */
    @VisibleForTesting
    WarningsHistoryPageEntryDecorator(final NewBuildPageFacade newBuildPage) {
        super();

        this.newBuildPage = newBuildPage;
    }

    @Override
    public boolean isApplicable(@NonNull final HistoryWidget<?, ?> widget, @NonNull final HistoricalBuild build) {
        return !findActions(build).isEmpty();
    }

    /**
     * Returns the warnings summary of the specified build. Since the decorator is a singleton that is shared by all
     * entries of the history, the build specific state is stored in the summary.
     *
     * @param build
     *         the build to get the summary for
     *
     * @return the summary, or an empty summary if the build has no static analysis results
     */
    public WarningsHistorySummary getSummary(final HistoricalBuild build) {
        var actions = findActions(build);
        if (actions.isEmpty()) {
            return WarningsHistorySummary.empty();
        }
        return WarningsHistorySummary.create(actions, getUrl(actions));
    }

    /**
     * Returns the URL of the summary, relative to the build. If the new build page is enabled, the summary links to
     * the Warnings tab. Otherwise, the summary links to the details of the result with the most warnings.
     *
     * @param actions
     *         the actions of the build, sorted by the number of warnings
     *
     * @return the URL
     */
    private String getUrl(final List<ResultAction> actions) {
        if (newBuildPage.isEnabled()) {
            return RunTab.URL + "/";
        }
        return actions.get(0).getUrlName() + "/";
    }

    /**
     * Returns the actions with the static analysis results of the specified build.
     *
     * @param build
     *         the build to get the actions for
     *
     * @return the actions, sorted by the number of warnings, or an empty list if the build is no {@link Run}
     */
    private List<ResultAction> findActions(final HistoricalBuild build) {
        if (build instanceof Run<?, ?> run) {
            return new RunTab(run).getWarningActions();
        }
        return List.of();
    }
}
