package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.HttpResponse;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import static edu.hm.hafner.analysis.assertj.Assertions.*;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.BuildIssue;
import io.jenkins.plugins.analysis.core.steps.PublishIssuesStep;
import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep;

import hudson.model.UnprotectedRootAction;
import hudson.util.HttpResponses;

/**
 * Integration tests of the warnings plug-in in pipelines.
 *
 * @author Ullrich Hafner
 * @see ScanForIssuesStep
 * @see PublishIssuesStep
 */
public class StepsITest extends PipelineITest {
    /**
     * Runs the Eclipse parser on the console log that contains 8 issues which are decorated with console notes. The
     * output file is copied to the console log using a shell cat command.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-11675">Issue 11675</a>
     */
    @Test
    public void issue11675() {
        WorkflowJob job = createJobWithWorkspaceFiles("issue11675.txt");
        String scanStep = String.format("def issues = scanForIssues tool: '%s'", Eclipse.ID);
        job.setDefinition(asStage("sh 'cat issue11675-issues.txt'", scanStep, PUBLISH_ISSUES_STEP));

        AnalysisResult result = scheduleBuild(job, Eclipse.ID);

        assertThat(result.getTotalSize()).isEqualTo(8);
        assertThat(result.getIssues()).hasSize(8);

        Issues<BuildIssue> issues = result.getIssues();
        assertThat(issues.filter(issue -> "eclipse".equals(issue.getOrigin()))).hasSize(8);
        for (Issue annotation : issues) {
            assertThat(annotation.getMessage()).matches("[a-zA-Z].*");
        }
    }

    /** Runs the all Java parsers on three output files: the build should report issues of all tools. */
    @Test
    public void shouldCombineIssuesOfSeveralFiles() {
        WorkflowJob job = createJobWithWorkspaceFiles("eclipse.txt", "javadoc.txt", "javac.txt");
        job.setDefinition(asStage(createScanForIssuesStep(Java.ID, "java"),
                createScanForIssuesStep(Eclipse.ID, "eclipse"),
                createScanForIssuesStep(JavaDoc.ID, "javadoc"),
                "publishIssues issues:[java, eclipse, javadoc]"));

        AnalysisResult result = scheduleBuild(job, "java");

        Issues<BuildIssue> issues = result.getIssues();
        assertThat(issues.filter(issue -> "eclipse".equals(issue.getOrigin()))).hasSize(8);
        assertThat(issues.filter(issue -> "java".equals(issue.getOrigin()))).hasSize(2);
        assertThat(issues.filter(issue -> "javadoc".equals(issue.getOrigin()))).hasSize(6);
        assertThat(issues.getToolNames()).containsExactlyInAnyOrder("java", "javadoc", "eclipse");
        assertThat(result.getIssues()).hasSize(8 + 2 + 6);
    }

    // TODO: testcase with id, id no name, id and name

    /**
     * Runs the Eclipse parser on an output file that contains several issues. Applies an include filter that selects
     * only one issue (in the file AttributeException.java).
     */
    @Test
    public void shouldIncludeJustOneFile() {
        WorkflowJob job = createJobWithWorkspaceFiles("eclipse.txt");
        job.setDefinition(asStage(createScanForIssuesStep(Eclipse.ID),
                "publishIssues issues:[issues],  "
                        + "filters:[[property: [$class: 'IncludeFile'], pattern: '.*AttributeException.*']]"));

        AnalysisResult result = scheduleBuild(job, Eclipse.ID);

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(result.getIssues()).hasSize(1);
    }

    /**
     * Verifies that parsers based on Digester are not vulnerable to an XXE attack. Previous versions allowed any user
     * with an ability to configure a job to read any file from the Jenkins Master (even on hardened systems where
     * execution on master is disabled).
     *
     * @see <a href="https://jenkins.io/security/advisory/2018-01-22/">Jenkins Security Advisory 2018-01-22</a>
     */
    @Test
    public void showPreventXxe_security656() throws Exception {
        String oobInUserContentLink = j.getURL() + "userContent/oob.xml";
        String triggerLink = j.getURL() + "triggerMe";

        String xxeFile = getClass().getResource("testXxe-xxe.xml").getFile();
        String xxeFileContent = FileUtils.readFileToString(new File(xxeFile), StandardCharsets.UTF_8);

        String oobFile = getClass().getResource("testXxe-oob.xml").getFile();
        String oobFileContent = FileUtils.readFileToString(new File(oobFile), StandardCharsets.UTF_8);

        File userContentDir = new File(j.jenkins.getRootDir(), "userContent");
        String adaptedOobFileContent = oobFileContent.replace("$TARGET_URL$", triggerLink);
        Files.write(new File(userContentDir, "oob.xml").toPath(), adaptedOobFileContent.getBytes());

        WorkflowJob job = createJob();
        job.setDefinition(asStage("def issues = scanForIssues tool: 'checkstyle', pattern:'xxe.xml'",
                "publishIssues issues:[issues]"));

        String adaptedXxeFileContent = xxeFileContent.replace("$OOB_LINK$", oobInUserContentLink);
        createFileInWorkspace(job, "xxe.xml", adaptedXxeFileContent);

        scheduleBuild(job, CheckStyle.ID);

        YouCannotTriggerMe urlHandler = j.jenkins.getExtensionList(UnprotectedRootAction.class)
                .get(YouCannotTriggerMe.class);
        assertThat(urlHandler).isNotNull();
        assertThat(urlHandler.triggerCount).as("XXE detected: URL has been triggered!").isEqualTo(0);
    }

    @TestExtension("testXxe")
    public static class YouCannotTriggerMe implements UnprotectedRootAction {
        private int triggerCount = 0;

        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getUrlName() {
            return "triggerMe";
        }

        public HttpResponse doIndex() {
            triggerCount++;
            return HttpResponses.plainText("triggered");
        }
    }

}
