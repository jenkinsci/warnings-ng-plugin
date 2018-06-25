package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;

import org.junit.Before;
import org.jvnet.hudson.test.ToolInstallations;

import hudson.FilePath;
import hudson.maven.MavenModuleSet;
import hudson.model.TopLevelItem;
import hudson.tasks.Maven.MavenInstallation;

/**
 * Integration tests of {@link MavenModuleSet} with {@link io.jenkins.plugins.analysis.core.quality.QualityGate}.
 *
 * @author Michaela Reitschuster
 */
public class MavenProjectQualityGateITest extends AbstractQualityGateITest<MavenModuleSet> {
    // FIXME: check if this can be done as @BeforeClass
    @Before
    public void initializeMaven() throws Exception {
        MavenInstallation mvn = ToolInstallations.configureMaven35();
    }

    @Override
    protected MavenModuleSet createProject() {
        MavenModuleSet project = createJob();
        copyToWorkspace(project, "pom.xml");
        return project;
    }

    private void copyToWorkspace(final TopLevelItem job, final String... fileNames) {
        try {
            FilePath workspace = j.jenkins.getWorkspaceFor(job);
            for (String fileName : fileNames) {
                workspace.child(fileName).copyFrom(asInputStream(fileName));
            }
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private MavenModuleSet createJob() {
        try {
            return j.createProject(MavenModuleSet.class);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

}
