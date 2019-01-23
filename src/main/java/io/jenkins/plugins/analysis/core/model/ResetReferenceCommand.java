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
 * FIXME: comment class.
 *
 * @author Ullrich Hafner
 */
public class ResetReferenceCommand {
    private JenkinsFacade jenkinsFacade = new JenkinsFacade();

    @VisibleForTesting
    void setJenkinsFacade(final JenkinsFacade jenkinsFacade) {
        this.jenkinsFacade = jenkinsFacade;
    }

    public void execute(final Run<?, ?> build, final String id) {
        if (isEnabled(build, id)) {
            resetReferenceBuild(build, id);
        }
    }

    @VisibleForTesting
    public void resetReferenceBuild(final Run<?, ?> build, final String id) {
        try {
            build.addOrReplaceAction(new ResetReferenceAction(id));
            build.save();
        }
        catch (IOException ignore) {
            // ignore
        }
    }

    public boolean isEnabled(final Run<?, ?> selectedBuild, final String id) {
        if (!jenkinsFacade.hasPermission(Item.CONFIGURE)) {
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
