package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Run;
import hudson.widgets.HistoryWidget;
import io.jenkins.plugins.analysis.core.model.RunTab.NewBuildPageFacade;
import java.util.List;
import jenkins.model.HistoricalBuild;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link WarningsHistoryPageEntryDecorator}.
 *
 * @author Ullrich Hafner
 */
class WarningsHistoryPageEntryDecoratorTest {
    private static final String CHECKSTYLE = "checkstyle";
    private static final String PMD = "pmd";

    @Test
    void shouldIgnoreBuildsThatAreNoRuns() {
        var decorator = createDecorator(true);
        var build = mock(HistoricalBuild.class);

        assertThat(decorator.isApplicable(mock(HistoryWidget.class), build)).isFalse();
        assertThat(decorator.getSummary(build)).isEmpty();
    }

    @Test
    void shouldIgnoreBuildsWithoutWarningsActions() {
        var decorator = createDecorator(true);
        var build = createBuild();

        assertThat(decorator.isApplicable(mock(HistoryWidget.class), build)).isFalse();
        assertThat(decorator.getSummary(build)).isEmpty();
    }

    @Test
    void shouldSumUpAllResults() {
        var decorator = createDecorator(true);
        var build = createBuild(createAction(CHECKSTYLE, 12, 4, 1), createAction(PMD, 3, 1, 2));

        assertThat(decorator.isApplicable(mock(HistoryWidget.class), build)).isTrue();

        assertThat(decorator.getSummary(build))
                .isNotEmpty()
                .hasUrl("warnings/")
                .hasIcon(RunTab.ICON)
                .hasTotalSize(15)
                .hasExistingSize(10)
                .hasNewSize(5)
                .hasFixedSize(3);
    }

    @Test
    void shouldShowBuildWithoutWarnings() {
        var decorator = createDecorator(true);
        var build = createBuild(createAction(CHECKSTYLE, 0, 0, 2));

        assertThat(decorator.isApplicable(mock(HistoryWidget.class), build)).isTrue();

        assertThat(decorator.getSummary(build))
                .isNotEmpty()
                .hasTotalSize(0)
                .hasExistingSize(0)
                .hasNewSize(0)
                .hasFixedSize(2);
    }

    @Test
    void shouldLinkToResultWithMostWarningsIfNewBuildPageIsDisabled() {
        var decorator = createDecorator(false);
        var build = createBuild(createAction(CHECKSTYLE, 1, 0, 0), createAction(PMD, 5, 0, 0));

        assertThat(decorator.getSummary(build)).hasUrl(PMD + "/");
    }

    /**
     * Creates a decorator with a stubbed experimental flag of the new build page.
     *
     * @param isNewBuildPageEnabled
     *         determines whether the new build page is enabled
     *
     * @return the decorator
     */
    private WarningsHistoryPageEntryDecorator createDecorator(final boolean isNewBuildPageEnabled) {
        var facade = mock(NewBuildPageFacade.class);
        when(facade.isEnabled()).thenReturn(isNewBuildPageEnabled);
        return new WarningsHistoryPageEntryDecorator(facade);
    }

    /**
     * Creates a build stub with the specified actions.
     *
     * @param actions
     *         the actions of the build
     *
     * @return the build stub
     */
    private Run<?, ?> createBuild(final ResultAction... actions) {
        Run<?, ?> run = mock(Run.class);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(actions));
        return run;
    }

    /**
     * Creates an action stub with a result that has the specified sizes.
     *
     * @param id
     *         the ID (and URL name) of the action
     * @param totalSize
     *         the total number of warnings
     * @param newSize
     *         the number of new warnings
     * @param fixedSize
     *         the number of fixed warnings
     *
     * @return the action stub
     */
    private ResultAction createAction(final String id, final int totalSize, final int newSize, final int fixedSize) {
        var action = mock(ResultAction.class);
        var result = mock(AnalysisResult.class);

        when(action.getResult()).thenReturn(result);
        when(action.getUrlName()).thenReturn(id);
        when(action.getDisplayName()).thenReturn(id);
        when(result.getTotalSize()).thenReturn(totalSize);
        when(result.getNewSize()).thenReturn(newSize);
        when(result.getFixedSize()).thenReturn(fixedSize);

        return action;
    }
}
