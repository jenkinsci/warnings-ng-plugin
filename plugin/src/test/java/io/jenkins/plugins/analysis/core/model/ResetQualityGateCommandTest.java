package io.jenkins.plugins.analysis.core.model;

import java.util.Collections;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Item;

import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.QualityGateStatus;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ResetQualityGateCommand}.
 *
 * @author Ullrich Hafner
 */
class ResetQualityGateCommandTest {
    private static final String ID = "id";

    @ParameterizedTest @EnumSource(value = QualityGateStatus.class, names = {"WARNING", "FAILED"})
    void shouldBeEnabledIfAllConditionsAreSatisfied(final QualityGateStatus qualityGateStatus) {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        FreeStyleProject parent = mock(FreeStyleProject.class);
        command.setJenkinsFacade(configureCorrectUserRights(true, parent));
        ResultAction resultAction = createResultAction(qualityGateStatus, ID);
        FreeStyleBuild selectedBuild = attachReferenceBuild(true, false, resultAction, parent);

        assertThat(command.isEnabled(selectedBuild, ID)).isTrue();
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void shouldBeEnabledIfUserHasLocalConfigureRights(final boolean hasConfigureRightOnBuild) {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        FreeStyleProject parent = mock(FreeStyleProject.class);
        command.setJenkinsFacade(configureCorrectUserRights(false, parent));
        ResultAction resultAction = createResultAction(QualityGateStatus.WARNING, ID);
        FreeStyleBuild selectedBuild = attachReferenceBuild(true, hasConfigureRightOnBuild, resultAction, parent);

        assertThat(command.isEnabled(selectedBuild, ID)).isEqualTo(hasConfigureRightOnBuild);
    }

    @Test
    void shouldBeDisabledIfActionAlreadyExists() {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        FreeStyleProject parent = mock(FreeStyleProject.class);
        command.setJenkinsFacade(configureCorrectUserRights(true, parent));
        ResultAction resultAction = createResultAction(QualityGateStatus.WARNING, ID);
        FreeStyleBuild selectedBuild = attachReferenceBuild(false, false, resultAction, parent);
        when(selectedBuild.getActions(ResultAction.class)).thenReturn(Lists.list(resultAction));

        assertThat(command.isEnabled(selectedBuild, ID)).isFalse();
    }

    @Test
    void shouldBeDisabledIfQualityGateIsSuccessful() {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        FreeStyleProject parent = mock(FreeStyleProject.class);
        command.setJenkinsFacade(configureCorrectUserRights(true, parent));
        ResultAction resultAction = createResultAction(QualityGateStatus.PASSED, ID);
        FreeStyleBuild selectedBuild = attachReferenceBuild(true, false, resultAction, parent);
        when(selectedBuild.getActions(ResultAction.class)).thenReturn(Lists.list(resultAction));

        assertThat(command.isEnabled(selectedBuild, ID)).isFalse();
    }

    @Test
    void shouldBeDisabledIfNoResultAction() {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        FreeStyleProject parent = mock(FreeStyleProject.class);
        command.setJenkinsFacade(configureCorrectUserRights(true, parent));
        FreeStyleBuild selectedBuild = mock(FreeStyleBuild.class);
        when(selectedBuild.getActions(ResetReferenceAction.class)).thenReturn(
                Lists.list(new ResetReferenceAction("other")));
        when(selectedBuild.getActions(ResultAction.class)).thenReturn(Collections.emptyList());

        assertThat(command.isEnabled(selectedBuild, ID)).isFalse();
    }

    @Test
    void shouldBeDisabledIfOtherResultAction() {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        FreeStyleProject parent = mock(FreeStyleProject.class);
        command.setJenkinsFacade(configureCorrectUserRights(true, parent));
        ResultAction resultAction = createResultAction(QualityGateStatus.WARNING, "other");
        FreeStyleBuild selectedBuild = attachReferenceBuild(true, false, resultAction, parent);
        when(selectedBuild.getActions(ResultAction.class)).thenReturn(Lists.list(resultAction));

        assertThat(command.isEnabled(selectedBuild, ID)).isFalse();
    }

    @Test
    void shouldBeDisabledIfNotLastResultAction() {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        FreeStyleProject parent = mock(FreeStyleProject.class);
        command.setJenkinsFacade(configureCorrectUserRights(true, parent));
        ResultAction resultAction = createResultAction(QualityGateStatus.WARNING, ID);
        FreeStyleBuild selectedBuild = attachReferenceBuild(true, false, resultAction, parent);
        FreeStyleBuild next = mock(FreeStyleBuild.class);
        when(selectedBuild.getNextBuild()).thenReturn(next);

        assertThat(command.isEnabled(selectedBuild, ID)).isFalse();
    }

    private JenkinsFacade configureCorrectUserRights(final boolean hasRight, final FreeStyleProject parent) {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.hasPermission(Item.CONFIGURE, parent)).thenReturn(hasRight);
        return jenkins;
    }

    private ResultAction createResultAction(final QualityGateStatus status, final String id) {
        ResultAction resultAction = mock(ResultAction.class);

        AnalysisResult result = mock(AnalysisResult.class);
        var qualityGateResult = mock(QualityGateResult.class);
        when(qualityGateResult.getOverallStatus()).thenReturn(status);
        when(qualityGateResult.isSuccessful()).thenReturn(status.isSuccessful());
        when(result.getQualityGateResult()).thenReturn(qualityGateResult);

        // Reference build is set
        FreeStyleBuild referenceBuild = mock(FreeStyleBuild.class);
        when(result.getReferenceBuild()).thenReturn(Optional.of(referenceBuild));

        when(resultAction.getResult()).thenReturn(result);
        when(resultAction.getId()).thenReturn(id);
        return resultAction;
    }

    private FreeStyleBuild attachReferenceBuild(final boolean hasNoReferenceBuild,
            final boolean hasConfigurePermission, final ResultAction resultAction, final FreeStyleProject parent) {
        FreeStyleBuild selectedBuild = mock(FreeStyleBuild.class);
        when(selectedBuild.getActions(ResetReferenceAction.class)).thenReturn(
                Lists.list(new ResetReferenceAction(hasNoReferenceBuild ? "other" : ID)));
        when(selectedBuild.getActions(ResultAction.class)).thenReturn(Lists.list(resultAction));
        when(selectedBuild.getAction(ResultAction.class)).thenReturn(resultAction);
        when(selectedBuild.hasPermission(Item.CONFIGURE)).thenReturn(hasConfigurePermission);
        when(selectedBuild.getParent()).thenReturn(parent);

        return selectedBuild;
    }
}
