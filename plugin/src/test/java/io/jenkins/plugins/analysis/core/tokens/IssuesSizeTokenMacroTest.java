package io.jenkins.plugins.analysis.core.tokens;

import org.junit.jupiter.api.Test;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.IssuesStatistics.StatisticProperties;

import static io.jenkins.plugins.analysis.core.testutil.JobStubs.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link IssuesSizeTokenMacro}.
 *
 * @author Ullrich Hafner
 */
class IssuesSizeTokenMacroTest {
    @Test
    void shouldReturnZeroIfNoActionPresent() {
        var macro = new IssuesSizeTokenMacro();

        Run<?, ?> run = createBuildWithActions();
        assertThat(expandMacro(macro, run)).isEqualTo("0");

        macro.setTool("id");
        assertThat(expandMacro(macro, run)).isEqualTo("0");
    }

    @Test
    void shouldExpandTokenOfSingleAction() {
        var macro = new IssuesSizeTokenMacro();

        Run<?, ?> run = createBuildWithActions(createAction("id", "name", 1));
        assertThat(expandMacro(macro, run)).isEqualTo("1");

        macro.setTool("id");
        assertThat(expandMacro(macro, run)).isEqualTo("1");

        macro.setTool("other");
        assertThat(expandMacro(macro, run)).isEqualTo("0");
    }

    @Test
    void shouldExpandTokenOfTwoActions() {
        var macro = new IssuesSizeTokenMacro();

        Run<?, ?> run = createBuildWithActions(
                createAction("first", "first name", 1),
                createAction("second", "second name", 2));
        assertThat(expandMacro(macro, run)).isEqualTo("3");

        macro.setTool("first");
        assertThat(expandMacro(macro, run)).isEqualTo("1");

        macro.setTool("second");
        assertThat(expandMacro(macro, run)).isEqualTo("2");

        macro.setTool("other");
        assertThat(expandMacro(macro, run)).isEqualTo("0");
    }

    @Test
    void shouldExpandTokenForNewAndFixedWarnings() {
        var macro = new IssuesSizeTokenMacro();

        Run<?, ?> run = createBuildWithActions(
                createAction("id", "name", 3, 2, 1));

        assertThat(expandMacro(macro, run)).isEqualTo("3");

        macro.setType(StatisticProperties.NEW.name());
        assertThat(expandMacro(macro, run)).isEqualTo("2");

        macro.setType(StatisticProperties.FIXED.name());
        assertThat(expandMacro(macro, run)).isEqualTo("1");
    }

    @Test
    void shouldThrowExceptionIfEnumDoesNotExist() {
        var macro = new IssuesSizeTokenMacro();

        assertThatIllegalArgumentException().isThrownBy(
                () -> macro.setType("wrong")).withMessageContaining("wrong");
    }

    private String expandMacro(final IssuesSizeTokenMacro macro, final Run<?, ?> run) {
        return macro.evaluate(run, null, null, null);
    }
}
