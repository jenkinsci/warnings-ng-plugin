package io.jenkins.plugins.analysis.core.tokens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.AbstractBuild;

/**
 * Tests the class {@link IssuesSizeTokenMacro}.
 *
 * @author Ullrich Hafner
 */
class IssuesSizeTokenMacroTest {
    @Test
    void shouldReturnZeroIfNoActionPresent() {
        IssuesSizeTokenMacro macro = new IssuesSizeTokenMacro();

        AbstractBuild<?, ?> run = createBuildWithNoActions();
        assertThat(macro.evaluate(run, null, null)).isEqualTo("0");

        macro.setTool("id");
        assertThat(macro.evaluate(run, null, null)).isEqualTo("0");
    }

    private AbstractBuild<?, ?> createBuildWithNoActions() {
        AbstractBuild<?, ?> run = mock(AbstractBuild.class);
        when(run.getActions(ResultAction.class)).thenReturn(Collections.emptyList());
        return run;
    }

    @Test
    void shouldExpandTokenOfSingleAction() {
        IssuesSizeTokenMacro macro = new IssuesSizeTokenMacro();

        AbstractBuild<?, ?> run = createBuildWithOneAction();
        assertThat(macro.evaluate(run, null, null)).isEqualTo("1");

        macro.setTool("id");
        assertThat(macro.evaluate(run, null, null)).isEqualTo("1");
        
        macro.setTool("other");
        assertThat(macro.evaluate(run, null, null)).isEqualTo("0");
    }

    @Test
    void shouldExpandTokenOfTwoActions() {
        IssuesSizeTokenMacro macro = new IssuesSizeTokenMacro();

        AbstractBuild<?, ?> run = createBuildWithTwoActions();
        assertThat(macro.evaluate(run, null, null)).isEqualTo("3");

        macro.setTool("first");
        assertThat(macro.evaluate(run, null, null)).isEqualTo("1");
        
        macro.setTool("second");
        assertThat(macro.evaluate(run, null, null)).isEqualTo("2");
        
        macro.setTool("other");
        assertThat(macro.evaluate(run, null, null)).isEqualTo("0");
    }

    private AbstractBuild<?, ?> createBuildWithTwoActions() {
        AbstractBuild<?, ?> run = mock(AbstractBuild.class);
        List<ResultAction> actions = new ArrayList<>();
        actions.add(createActionWithResult("first", 1));
        actions.add(createActionWithResult("second", 2));
        when(run.getActions(ResultAction.class)).thenReturn(actions);
        return run;
    }

    private AbstractBuild<?, ?> createBuildWithOneAction() {
        AbstractBuild<?, ?> run = mock(AbstractBuild.class);
        ResultAction action = createActionWithResult("id", 1);
        when(run.getActions(ResultAction.class)).thenReturn(Collections.singletonList(action));
        return run;
    }

    private ResultAction createActionWithResult(final String id, final int issuesCount) {
        ResultAction action = mock(ResultAction.class);
        when(action.getId()).thenReturn(id);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getTotalSize()).thenReturn(issuesCount);
        when(action.getResult()).thenReturn(result);
        return action;
    }
}