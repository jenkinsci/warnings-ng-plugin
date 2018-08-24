package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;

import hudson.model.FreeStyleProject;

/**
 * Integration tests of {@link FreeStyleProject} with {@link io.jenkins.plugins.analysis.core.quality.QualityGate}.
 *
 * @author Michaela Reitschuster
 */
public class FreeStyleProjectQualityGateITest extends AbstractQualityGateITest<FreeStyleProject> {
    @Override
    protected FreeStyleProject createProject() {
        try {
            return getJenkins().createFreeStyleProject();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
