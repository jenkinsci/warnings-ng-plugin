package io.jenkins.plugins.analysis.core.model;

import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import hudson.model.FreeStyleBuild;
import hudson.model.Item;

import io.jenkins.plugins.analysis.core.util.JenkinsFacade;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ResetQualityGateCommand}.
 *
 * @author Ullrich Hafner
 */
class ResetQualityGateCommandTest {
    private static final String ID = "id";

    @Test
    void shouldBeEnabledIfAllConditionsAreSatisfied() {
        verifyEnabledWithQualityGateStatus(QualityGateStatus.WARNING);
        verifyEnabledWithQualityGateStatus(QualityGateStatus.FAILED);
    }

    private void verifyEnabledWithQualityGateStatus(final QualityGateStatus status) {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        command.setJenkinsFacade(configureCorrectUserRights(true));
        ResultAction resultAction = createResultAction(status, ID);
        FreeStyleBuild selectedBuild = attachReferenceBuild(true, resultAction);

        assertThat(command.isEnabled(selectedBuild, ID)).isTrue();
    }

    @Test
    void shouldBeDisabledIfUserHasNoConfigureRights() {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        command.setJenkinsFacade(configureCorrectUserRights(false));
        ResultAction resultAction = createResultAction(QualityGateStatus.WARNING, ID);
        FreeStyleBuild selectedBuild = attachReferenceBuild(true, resultAction);

        assertThat(command.isEnabled(selectedBuild, ID)).isFalse();
    }

    @Test
    void shouldBeDisabledIfActionAlreadyExists() {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        command.setJenkinsFacade(configureCorrectUserRights(true));
        ResultAction resultAction = createResultAction(QualityGateStatus.WARNING, ID);
        FreeStyleBuild selectedBuild = attachReferenceBuild(false, resultAction);
        when(selectedBuild.getActions(ResultAction.class)).thenReturn(Lists.list(resultAction));

        assertThat(command.isEnabled(selectedBuild, ID)).isFalse();
    }

    @Test
    void shouldBeDisabledIfQualityGateIsSuccessful() {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        command.setJenkinsFacade(configureCorrectUserRights(true));
        ResultAction resultAction = createResultAction(QualityGateStatus.PASSED, ID);
        FreeStyleBuild selectedBuild = attachReferenceBuild(true, resultAction);
        when(selectedBuild.getActions(ResultAction.class)).thenReturn(Lists.list(resultAction));

        assertThat(command.isEnabled(selectedBuild, ID)).isFalse();
    }

    @Test
    void shouldBeDisabledIfNoResultAction() {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        command.setJenkinsFacade(configureCorrectUserRights(true));
        FreeStyleBuild selectedBuild = mock(FreeStyleBuild.class);
        when(selectedBuild.getActions(ResetReferenceAction.class)).thenReturn(
                Lists.list(new ResetReferenceAction("other")));
        when(selectedBuild.getActions(ResultAction.class)).thenReturn(Lists.emptyList());

        assertThat(command.isEnabled(selectedBuild, ID)).isFalse();
    }

    @Test
    void shouldBeDisabledIfOtherResultAction() {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        command.setJenkinsFacade(configureCorrectUserRights(true));
        ResultAction resultAction = createResultAction(QualityGateStatus.WARNING, "other");
        FreeStyleBuild selectedBuild = attachReferenceBuild(true, resultAction);
        when(selectedBuild.getActions(ResultAction.class)).thenReturn(Lists.list(resultAction));

        assertThat(command.isEnabled(selectedBuild, ID)).isFalse();
    }

    @Test
    void shouldBeDisabledIfNotLastResultAction() {
        ResetQualityGateCommand command = new ResetQualityGateCommand();

        command.setJenkinsFacade(configureCorrectUserRights(true));
        ResultAction resultAction = createResultAction(QualityGateStatus.WARNING, ID);
        FreeStyleBuild selectedBuild = attachReferenceBuild(true, resultAction);
        FreeStyleBuild next = mock(FreeStyleBuild.class);
        when(selectedBuild.getNextBuild()).thenReturn(next);
        
        assertThat(command.isEnabled(selectedBuild, ID)).isFalse();
    }

    private JenkinsFacade configureCorrectUserRights(final boolean hasRight) {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.hasPermission(Item.CONFIGURE)).thenReturn(hasRight);
        return jenkins;
    }

    private ResultAction createResultAction(final QualityGateStatus status, final String id) {
        ResultAction resultAction = mock(ResultAction.class);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getQualityGateStatus()).thenReturn(status);

        // Reference build is set
        FreeStyleBuild referenceBuild = mock(FreeStyleBuild.class);
        when(result.getReferenceBuild()).thenReturn(Optional.of(referenceBuild));

        when(resultAction.getResult()).thenReturn(result);
        when(resultAction.getId()).thenReturn(id);
        return resultAction;
    }

    private FreeStyleBuild attachReferenceBuild(final boolean hasNoReferenceBuild,
            final ResultAction resultAction) {
        FreeStyleBuild selectedBuild = mock(FreeStyleBuild.class);
        when(selectedBuild.getActions(ResetReferenceAction.class)).thenReturn(
                Lists.list(new ResetReferenceAction(hasNoReferenceBuild ? "other" : ID)));
        when(selectedBuild.getActions(ResultAction.class)).thenReturn(Lists.list(resultAction));
        when(selectedBuild.getAction(ResultAction.class)).thenReturn(resultAction);
        return selectedBuild;
    }
}