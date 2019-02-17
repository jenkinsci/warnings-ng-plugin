package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import edu.hm.hafner.util.VisibleForTesting;

import hudson.model.Item;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.JenkinsFacade;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;

/**
 * Resets the quality gate of a static analysis tool to a clean state. Provides a manual way to restart the new warnings
 * quality gate evaluation from a clean state (without new warnings). I.e., once this command has been started the next
 * build (started automatically or manually) will not evaluate a quality gate that considers the number of new warnings.
 * This helps to start over from a clean state if the number of new warnings have been increased accidentally. Otherwise
 * you won't get a successful build anymore until all new warnings have been fixed.
 * <p>
 * Technically, this command just adds a marker action to the selected build. Once a new build is running, this marker
 * action will be checked for existence and the quality gate will be bypassed for one single build.
 * </p>
 *
 * @author Ullrich Hafner
 */
public class ResetQualityGateCommand {
    private JenkinsFacade jenkinsFacade = new JenkinsFacade();

    @VisibleForTesting
    void setJenkinsFacade(final JenkinsFacade jenkinsFacade) {
        this.jenkinsFacade = jenkinsFacade;
    }

    /**
     * Resets the quality gate for the specified analysis tool. This command does not run if not enabled.
     *
     * @param selectedBuild
     *         the selected build that will show the action link
     * @param id
     *         the ID of the static analysis tool that should be reset
     *
     * @see #isEnabled(Run, String)
     */
    public void execute(final Run<?, ?> selectedBuild, final String id) {
        if (isEnabled(selectedBuild, id)) {
            resetReferenceBuild(selectedBuild, id);
        }
    }

    /**
     * Resets the quality gate for the specified analysis tool.
     *
     * @param selectedBuild
     *         the selected build that will show the action link
     * @param id
     *         the ID of the static analysis tool that should be reset
     */
    @VisibleForTesting
    public void resetReferenceBuild(final Run<?, ?> selectedBuild, final String id) {
        try {
            selectedBuild.addAction(new ResetReferenceAction(id));
            selectedBuild.save();
        }
        catch (IOException ignore) {
            // ignore
        }
    }

    /**
     * Returns whether the command to reset the quality gate for the specified analysis tool is enabled or not.
     *
     * @param selectedBuild
     *         the selected build that will show the action link
     * @param id
     *         the ID of the static analysis tool that should be reset
     *
     * @return {@code true} if the command is enabled, {@code false} otherwise
     */
    public boolean isEnabled(final Run<?, ?> selectedBuild, final String id) {
        if (!jenkinsFacade.hasPermission(Item.CONFIGURE)) {
            return false;
        }

        if (selectedBuild.getNextBuild() != null) {
            return false;
        }

        List<ResetReferenceAction> actions = selectedBuild.getActions(ResetReferenceAction.class);
        if (actions.stream().map(ResetReferenceAction::getId).anyMatch(id::equals)) {
            return false;
        }

        Optional<ResultAction> resultAction = selectedBuild.getActions(ResultAction.class)
                .stream()
                .filter(action -> action.getId().equals(id))
                .findAny();
        if (!resultAction.isPresent()) {
            return false;
        }

        QualityGateStatus status = resultAction.get().getResult().getQualityGateStatus();
        return status == QualityGateStatus.FAILED || status == QualityGateStatus.WARNING;
    }
}
