package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link MissingResultFallbackHandler}.
 *
 * @author Akash Manna
 */
class MissingResultFallbackHandlerTest {
    private static final String CHECKSTYLE_ID = "checkstyle";
    private static final String CHECKSTYLE_NAME = "CheckStyle";
    private static final String SPOTBUGS_ID = "spotbugs";
    private static final String SPOTBUGS_NAME = "SpotBugs";

    /**
     * Verifies that duplicate JobActions from multiple ResultActions with the same
     * tool ID are properly deduplicated.
     * This test validates the fix for JENKINS-75748, where multiple ResultActions
     * with the same tool ID in a previous
     * build would cause duplicate warning tabs in the job view.
     */
    @Test
    void shouldDeduplicateJobActionsFromMultipleResultActions() {
        var handler = new MissingResultFallbackHandler();

        Job<?, ?> job = mock(Job.class);

        Run<?, ?> currentBuild = mock(Run.class);
        when(currentBuild.isBuilding()).thenReturn(false);
        when(currentBuild.getActions(ResultAction.class)).thenReturn(new ArrayList<>());
        when(job.getLastBuild()).thenAnswer(i -> currentBuild);

        Run<?, ?> previousBuild = mock(Run.class);

        ResultAction action1 = createResultAction(CHECKSTYLE_ID, CHECKSTYLE_NAME);

        ResultAction action2 = createResultAction(CHECKSTYLE_ID, CHECKSTYLE_NAME);

        List<ResultAction> resultActions = List.of(action1, action2);
        when(previousBuild.getActions(ResultAction.class)).thenReturn(resultActions);
        when(currentBuild.getPreviousBuild()).thenAnswer(i -> previousBuild);
        when(previousBuild.getPreviousBuild()).thenAnswer(i -> null);

        Collection<? extends Action> actions = handler.createFor(job);

        assertThat(actions).hasSize(1);

        JobAction jobAction = (JobAction) actions.iterator().next();
        assertThat(jobAction.getUrlName()).isEqualTo(CHECKSTYLE_ID);
    }

    /**
     * Verifies that different tools are not deduplicated.
     */
    @Test
    void shouldNotDeduplicateJobActionsFromDifferentTools() {
        var handler = new MissingResultFallbackHandler();

        Job<?, ?> job = mock(Job.class);

        Run<?, ?> currentBuild = mock(Run.class);
        when(currentBuild.isBuilding()).thenReturn(false);
        when(currentBuild.getActions(ResultAction.class)).thenReturn(new ArrayList<>());
        when(job.getLastBuild()).thenAnswer(i -> currentBuild);

        Run<?, ?> previousBuild = mock(Run.class);

        ResultAction checkstyleAction = createResultAction(CHECKSTYLE_ID, CHECKSTYLE_NAME);
        ResultAction spotbugsAction = createResultAction(SPOTBUGS_ID, SPOTBUGS_NAME);

        List<ResultAction> resultActions = List.of(checkstyleAction, spotbugsAction);
        when(previousBuild.getActions(ResultAction.class)).thenReturn(resultActions);
        when(currentBuild.getPreviousBuild()).thenAnswer(i -> previousBuild);
        when(previousBuild.getPreviousBuild()).thenAnswer(i -> null);

        Collection<? extends Action> actions = handler.createFor(job);

        assertThat(actions).hasSize(2);

        List<String> urlNames = actions.stream()
                .filter(a -> a instanceof JobAction)
                .map(a -> ((JobAction) a).getUrlName())
                .toList();

        assertThat(urlNames).containsExactlyInAnyOrder(CHECKSTYLE_ID, SPOTBUGS_ID);
    }

    /**
     * Verifies that the handler returns empty list when current build has results.
     */
    @Test
    void shouldReturnEmptyListWhenCurrentBuildHasResults() {
        var handler = new MissingResultFallbackHandler();

        Job<?, ?> job = mock(Job.class);

        Run<?, ?> currentBuild = mock(Run.class);
        when(currentBuild.isBuilding()).thenReturn(false);

        ResultAction currentAction = createResultAction(CHECKSTYLE_ID, CHECKSTYLE_NAME);
        when(currentBuild.getActions(ResultAction.class)).thenReturn(List.of(currentAction));
        when(job.getLastBuild()).thenAnswer(i -> currentBuild);

        Collection<? extends Action> actions = handler.createFor(job);

        assertThat(actions).isEmpty();
    }

    /**
     * Verifies that the handler returns empty list when no builds exist.
     */
    @Test
    void shouldReturnEmptyListWhenNoBuildExists() {
        var handler = new MissingResultFallbackHandler();

        Job<?, ?> job = mock(Job.class);
        when(job.getLastBuild()).thenReturn(null);

        Collection<? extends Action> actions = handler.createFor(job);

        assertThat(actions).isEmpty();
    }

    /**
     * Creates a mock {@link ResultAction} that returns a {@link JobAction}.
     *
     * @param id   the ID of the tool
     * @param name the name of the tool
     * @return the mocked ResultAction
     */
    private ResultAction createResultAction(final String id, final String name) {
        ResultAction action = mock(ResultAction.class);
        when(action.getId()).thenReturn(id);
        when(action.getDisplayName()).thenReturn(name);

        JobAction jobAction = mock(JobAction.class);
        when(jobAction.getUrlName()).thenReturn(id);

        doReturn(List.of(jobAction)).when(action).getProjectActions();

        return action;
    }
}
