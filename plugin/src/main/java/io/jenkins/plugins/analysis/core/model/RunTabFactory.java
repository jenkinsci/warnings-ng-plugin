package io.jenkins.plugins.analysis.core.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import jenkins.model.Tab;
import jenkins.model.TransientActionFactory;

import java.util.Collection;
import java.util.List;

/**
 * Adds a Warnings tab to the run page.
 */
@Extension
public class RunTabFactory extends TransientActionFactory<Run> {
    @Override
    public Class<Run> type() {
        return Run.class;
    }

    @NonNull
    @Override
    public Collection<? extends Tab> createFor(@NonNull final Run target) {
        return List.of(new RunTab(target));
    }
}
