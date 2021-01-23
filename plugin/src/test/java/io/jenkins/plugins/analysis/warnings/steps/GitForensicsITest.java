package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.Test;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Verifies that Git blamer and miner are correctly called.
 *
 * @author Ullrich Hafner
 */
public class GitForensicsITest extends IntegrationTestWithJenkinsPerTest {

    private static final String JAVA_ONE_WARNING = "java-start-rev0.txt";
    private static final String DISCOVER_REFERENCE_BUILD_STEP = "discoverReferenceBuild(referenceJob:'reference')";
    private static final String PUBLISH_ISSUES_STEP = "publishIssues issues:[issues]";
    private static final String CHECKOUT_FORENSICS_API = "checkout([$class: 'GitSCM', "
            + "branches: [[name: 'a6d0ef09ab3c418e370449a884da99b8190ae950' ]],\n"
            + "userRemoteConfigs: [[url: 'https://github.com/jenkinsci/forensics-api-plugin.git']],\n"
            + "extensions: [[$class: 'RelativeTargetDirectory', \n"
            + "            relativeTargetDir: 'forensics-api']]])";
    private static final String MINE_REPOSITORY = "mineRepository()";

    /**
     * Checks out an existing Git repository and starts a pipeline. Verifies that the Git forensics plugin is correctly
     * invoked.
     */
    @Test
    public void shouldObtainBlamesAndForensics() {
        WorkflowJob job = createPipelineWithWorkspaceFiles();

        createFileInWorkspace(job, "java-issues.txt",
                createJavaWarning("src/main/java/io/jenkins/plugins/forensics/util/ScmResolver.java", 20)
        );

        job.setDefinition(asStage(CHECKOUT_FORENSICS_API, MINE_REPOSITORY,
                "def issues = scanForIssues "
                        + "sourceDirectory: 'forensics-api', "
                        + "tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8')",
                PUBLISH_ISSUES_STEP));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(1).hasNewSize(0).hasFixedSize(0);

        assertThat(getConsoleLog(result)).contains(
                "Invoking Git blamer to create author and commit information for 1 affected files",
                "Git commit ID = 'a6d0ef09ab3c418e370449a884da99b8190ae950'",
                "-> blamed authors of issues in 1 files",
                "Extracting repository forensics for 1 affected files (files in repository: 121)",
                "-> 1 affected files processed");
    }

    /**
     * Checks out an existing Git repository and starts a pipeline. Verifies that the Git forensics plugin is correctly
     * invoked.
     */
    @Test
    public void shouldSkipBlamesAndForensics() {
        WorkflowJob job = createPipelineWithWorkspaceFiles(JAVA_ONE_WARNING);
        job.setDefinition(asStage(CHECKOUT_FORENSICS_API, MINE_REPOSITORY,
                "def issues = scanForIssues "
                        + "sourceDirectory: 'forensics-api',"
                        + "scm: 'nothing', "
                        + "tool: java(pattern:'**/*issues.txt', reportEncoding:'UTF-8')",
                PUBLISH_ISSUES_STEP));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(1).hasNewSize(0).hasFixedSize(0);

        assertThat(getConsoleLog(result)).contains(
                "Creating SCM blamer to obtain author and commit information for affected files",
                "-> Filtering SCMs by key 'nothing'",
                "-> no SCM found",
                "Extracting repository forensics for 1 affected files (files in repository: 0)",
                "-> 0 affected files processed");
    }
}
