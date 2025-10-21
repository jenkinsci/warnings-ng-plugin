package io.jenkins.plugins.analysis.core.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import jenkins.model.Tab;
import jenkins.model.TransientActionFactory;
import jenkins.model.experimentalflags.NewBuildPageUserExperimentalFlag;

import java.util.Collection;
import java.util.Collections;

@Extension
public class RunTabFactory extends TransientActionFactory<Run> {

    @Override
    public Class<Run> type() {
        return Run.class;
    }

    @NonNull
    @Override
    public Collection<? extends Tab> createFor(@NonNull Run target) {
        boolean isExperimentalUiEnabled = new NewBuildPageUserExperimentalFlag().getFlagValue();

        if (!isExperimentalUiEnabled) {
            return Collections.emptySet();
        }

        return Collections.singleton(new RunTab(target));
    }
}
