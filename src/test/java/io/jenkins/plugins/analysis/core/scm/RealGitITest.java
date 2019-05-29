package io.jenkins.plugins.analysis.core.scm;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.CreateFileBuilder;

import hudson.model.FreeStyleProject;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.impl.RelativeTargetDirectory;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMHead;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Doxygen;

import static org.assertj.core.api.Assertions.*;

public class RealGitITest extends IntegrationTestWithJenkinsPerSuite {
    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Test
    public void gitBlameOnDifferentDirectoryIssue57260FreeStyleProject() {
        gitInitIssue57260();

        GitSCMBuilder builder = new GitSCMBuilder(new SCMHead("master"), null, sampleRepo.fileUrl(), null);
        RelativeTargetDirectory extension = new RelativeTargetDirectory("src"); // JENKINS-57260
        builder.withExtension(extension);
        GitSCM git = builder.build();

        FreeStyleProject project = createFreeStyleProject();

        try {
            project.setScm(git);
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        project.getBuildersList().add(new CreateFileBuilder("build/doxygen/doxygen/doxygen.log",
                "Notice: Output directory `build/doxygen/doxygen' does not exist. I have created it for you.\n"
                        + "src/CentralDifferenceSolver.cpp:11: Warning: reached end of file while inside a dot block!\n"
                        + "The command that should end the block seems to be missing!\n"
                        + " \n"
                        + "src/LCPcalc.cpp:12: Warning: the name `lcp_lexicolemke.c' supplied as the second argument in the \\file statement is not an input file"));


        IssuesRecorder recorder = enableWarnings(project, createTool(new Doxygen(), "build/doxygen/doxygen/doxygen.log"));
        recorder.setAggregatingResults(true);
        recorder.setEnabledForFailure(true);


        scheduleSuccessfulBuild(project);
        AnalysisResult result = scheduleSuccessfulBuild(project);

        assertThat(result.getErrorMessages()).doesNotContain("Can't determine head commit using 'git rev-parse'. Skipping blame.");
    }

    private void gitInitIssue57260() {
        try {
            sampleRepo.init();
            sampleRepo.write("CentralDifferenceSolver.cpp", "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12");
            sampleRepo.write("LCPcalc.cpp", "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13");
            sampleRepo.git("add", "CentralDifferenceSolver.cpp", "LCPcalc.cpp");
            sampleRepo.git("commit", "--all", "--message=init");
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
