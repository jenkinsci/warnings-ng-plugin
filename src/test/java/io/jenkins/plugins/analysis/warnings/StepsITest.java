package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.collections.impl.factory.Lists;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assume;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.HttpResponse;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import static edu.hm.hafner.analysis.assertj.Assertions.*;
import static hudson.Functions.*;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.steps.PublishIssuesStep;
import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import io.jenkins.plugins.analysis.warnings.groovy.GroovyParser;
import io.jenkins.plugins.analysis.warnings.groovy.ParserConfiguration;

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
        Assume.assumeFalse("Test not yet OS independent: requires UNIX commands", isWindows());

        WorkflowJob job = createJobWithWorkspaceFiles("issue11675.txt");
        job.setDefinition(asStage(
                "sh 'cat issue11675-issues.txt'",
                "def issues = scanForIssues tool: [$class: 'Eclipse']",
                PUBLISH_ISSUES_STEP));

        AnalysisResult result = scheduleBuild(job, Eclipse.class);

        assertThat(result.getTotalSize()).isEqualTo(8);
        assertThat(result.getIssues()).hasSize(8);

        Issues issues = result.getIssues();
        assertThat(issues.filter(issue -> "eclipse".equals(issue.getOrigin()))).hasSize(8);
        for (Issue annotation : issues) {
            assertThat(annotation.getMessage()).matches("[a-zA-Z].*");
        }
    }

    /** Runs the all Java parsers on three output files: the build should report issues of all tools. */
    @Test
    public void shouldCombineIssuesOfSeveralFiles() {
        publishResultsWithIdAndName(
                "publishIssues issues:[java, eclipse, javadoc]",
                "java", "Java Warnings");
    }

    /**
     * Runs the all Java parsers on three output files: the build should report issues of all tools. The results should
     * be aggregated into a new action with the specified ID. Since no name is given the default name is used.
     */
    @Test
    public void shouldProvideADefaultNameIfNoOneIsGiven() {
        publishResultsWithIdAndName(
                "publishIssues issues:[java, eclipse, javadoc], id:'my-id'",
                "my-id", "Static Analysis Warnings");
    }

    /**
     * Runs the all Java parsers on three output files: the build should report issues of all tools. The results should
     * be aggregated into a new action with the specified ID and the specified name.
     */
    @Test
    public void shouldUseSpecifiedName() {
        publishResultsWithIdAndName(
                "publishIssues issues:[java, eclipse, javadoc], id:'my-id', name:'my-name'",
                "my-id", "my-name");
    }

    private void publishResultsWithIdAndName(final String publishStep, final String expectedId,
            final String expectedName) {
        WorkflowJob job = createJobWithWorkspaceFiles("eclipse.txt", "javadoc.txt", "javac.txt");
        job.setDefinition(asStage(createScanForIssuesStep(Java.class, "java"),
                createScanForIssuesStep(Eclipse.class, "eclipse"),
                createScanForIssuesStep(JavaDoc.class, "javadoc"),
                publishStep));

        WorkflowRun run = runSuccessfully(job);

        ResultAction action = getResultAction(run);
        assertThat(action.getId()).isEqualTo(expectedId);
        assertThat(action.getDisplayName()).contains(expectedName);

        assertThatJavaIssuesArePublished(action.getResult());
    }

    private void assertThatJavaIssuesArePublished(final AnalysisResult result) {
        Issues issues = result.getIssues();
        assertThat(issues.filter(issue -> "eclipse".equals(issue.getOrigin()))).hasSize(8);
        assertThat(issues.filter(issue -> "java".equals(issue.getOrigin()))).hasSize(2);
        assertThat(issues.filter(issue -> "javadoc".equals(issue.getOrigin()))).hasSize(6);
        assertThat(issues.getTools()).containsExactlyInAnyOrder("java", "javadoc", "eclipse");
        assertThat(result.getIssues()).hasSize(8 + 2 + 6);
    }

    /**
     * Runs the Java parser on an pep8 log file: the build should report no issues. A result should be available with
     * the java ID and name.
     */
    @Test
    public void shouldHaveActionWithIdAndNameWithEmptyResults() {
        WorkflowJob job = createJobWithWorkspaceFiles("pep8Test.txt");
        job.setDefinition(asStage(createScanForIssuesStep(Java.class, "java"),
                "publishIssues issues:[java]"));

        WorkflowRun run = runSuccessfully(job);

        ResultAction action = getResultAction(run);
        assertThat(action.getId()).isEqualTo("java");
        assertThat(action.getDisplayName()).contains(Messages.Warnings_JavaParser_ParserName());

        AnalysisResult result = action.getResult();
        assertThat(result.getIssues()).isEmpty();
    }

    /**
     * Registers a new {@link GroovyParser} (a Pep8 parser) in Jenkins global configuration and runs this parser on an
     * error log with 8 issues.
     */
    @Test
    public void shouldShowWarningsOfGroovyParser() {
        WorkflowJob job = createJobWithWorkspaceFiles("pep8Test.txt");
        job.setDefinition(asStage(
                String.format("def groovy = scanForIssues tool: [$class: 'GroovyScript', id:'groovy-pep8'], "
                        + "pattern:'**/*issues.txt', defaultEncoding:'UTF-8'"),
                "publishIssues issues:[groovy]"));

        ParserConfiguration configuration = ParserConfiguration.getInstance();
        configuration.setParsers(Collections.singletonList(
                new GroovyParser("groovy-pep8", "Groovy Pep8",
                        "(.*):(\\d+):(\\d+): (\\D\\d*) (.*)",
                        toString("groovy/pep8.groovy"), "")));
        WorkflowRun run = runSuccessfully(job);

        ResultAction action = getResultAction(run);
        assertThat(action.getId()).isEqualTo("groovy-pep8");
        assertThat(action.getDisplayName()).contains("Groovy Pep8");

        AnalysisResult result = action.getResult();
        assertThat(result.getIssues()).hasSize(8);

        // FIXME: issues from the action should also have an ID
    }

    /**
     * Runs the Eclipse parser on an output file that contains several issues. Applies an include filter that selects
     * only one issue (in the file AttributeException.java).
     */
    @Test
    public void shouldIncludeJustOneFile() {
        WorkflowJob job = createJobWithWorkspaceFiles("eclipse.txt");
        job.setDefinition(asStage(createScanForIssuesStep(Eclipse.class),
                "publishIssues issues:[issues],  "
                        + "filters:[[property: [$class: 'IncludeFile'], pattern: '.*AttributeException.*']]"));

        AnalysisResult result = scheduleBuild(job, Eclipse.class);

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(result.getIssues()).hasSize(1);
    }

    // TODO: check all variants of a reference (non-existing name, no run in job, overallResultMustBeSuccess, ignoreAnalysisResult, etc.)
    /**
     * Creates a reference job and starts the analysis for this job. Then another job is created that uses the first
     * one as reference. Verifies that the association is correctly stored.
     */
    @Test
    public void shouldUseOtherJobAsReference() {
        WorkflowJob reference = createJob("reference");
        copyFilesToWorkspace(reference, "java-start.txt");
        reference.setDefinition(asStage(createScanForIssuesStep(Java.class), PUBLISH_ISSUES_STEP));

        AnalysisResult referenceResult = scheduleBuild(reference, Java.class);

        assertThat(referenceResult.getTotalSize()).isEqualTo(2);
        assertThat(referenceResult.getIssues()).hasSize(2);
        assertThat(referenceResult.getReferenceBuild()).isEmpty();

        WorkflowJob job = createJobWithWorkspaceFiles("java-start.txt");
        job.setDefinition(asStage(createScanForIssuesStep(Java.class),
                "publishIssues issues:[issues], referenceJobName:'reference'"));

        AnalysisResult result = scheduleBuild(reference, Java.class);

        assertThat(result.getTotalSize()).isEqualTo(2);
        assertThat(result.getIssues()).hasSize(2);
        assertThat(result.getReferenceBuild()).hasValue(referenceResult.getOwner());

        // TODO: add verification for io.jenkins.plugins.analysis.core.model.IssueDifference
    }

    /**
     * Verifies that parsers based on Digester are not vulnerable to an XXE attack. Previous versions allowed any user
     * with an ability to configure a job to read any file from the Jenkins Master (even on hardened systems where
     * execution on master is disabled).
     *
     * @see <a href="https://jenkins.io/security/advisory/2018-01-22/">Jenkins Security Advisory 2018-01-22</a>
     */
    @Test
    public void showPreventXxeSecurity656() throws Exception {
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
        String adaptedXxeFileContent = xxeFileContent.replace("$OOB_LINK$", oobInUserContentLink);
        createFileInWorkspace(job, "xxe.xml", adaptedXxeFileContent);

        List<Class<? extends StaticAnalysisTool>> classes = Lists.mutable.of(CheckStyle.class, Pmd.class,
                FindBugs.class, JcReport.class);
        for (Class<? extends StaticAnalysisTool> tool : classes) {
            job.setDefinition(asStage(
                    String.format("def issues = scanForIssues tool: [$class: '%s'], pattern:'xxe.xml'",
                            tool.getSimpleName()),
                    "publishIssues issues:[issues]"));

            scheduleBuild(job, tool);

            YouCannotTriggerMe urlHandler = j.jenkins.getExtensionList(UnprotectedRootAction.class)
                    .get(YouCannotTriggerMe.class);
            assertThat(urlHandler).isNotNull();

            assertThat(urlHandler.triggerCount).as("XXE detected for parser %s: URL has been triggered!", tool)
                    .isEqualTo(0);
        }
    }

    @TestExtension
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
