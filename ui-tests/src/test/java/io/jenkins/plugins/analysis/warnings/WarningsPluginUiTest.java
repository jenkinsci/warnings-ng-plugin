package io.jenkins.plugins.analysis.warnings;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaGitContainer;
import org.jenkinsci.test.acceptance.junit.WithCredentials;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.ssh_slaves.SshSlaveLauncher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Slave;

import io.jenkins.plugins.analysis.warnings.AnalysisResult.Tab;
import io.jenkins.plugins.analysis.warnings.AnalysisSummary.QualityGateResult;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder.QualityGateCriticality;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder.QualityGateType;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * Acceptance tests for the Warnings Next Generation Plugin.
 *
 * @author Frank Christian Geyer
 * @author Ullrich Hafner
 * @author Manuel Hampp
 * @author Anna-Maria Hardi
 * @author Elvira Hauer
 * @author Deniz Mardin
 * @author Stephan Plöderl
 * @author Alexander Praegla
 * @author Michaela Reitschuster
 * @author Arne Schöntag
 * @author Alexandra Wenzel
 * @author Nikolai Wohlgemuth
 * @author Florian Hageneder
 * @author Veronika Zwickenpflug
 */
@WithPlugins("warnings-ng")
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "PMD.SystemPrintln", "PMD.ExcessiveImports"})
@SuppressFBWarnings("BC")
public class WarningsPluginUiTest extends UiTest {
    private static final String SOURCE_VIEW_FOLDER = "/source-view/";

    /**
     * Credentials to access the docker container. The credentials are stored with the specified ID and use the provided
     * SSH key. Use the following annotation on your test case to use the specified docker container as git server or
     * build agent:
     * <blockquote>
     * <pre>@Test @WithDocker @WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY,
     *                                    values = {CREDENTIALS_ID, CREDENTIALS_KEY})}
     * public void shouldTestWithDocker() {
     * }
     * </pre></blockquote>
     */
    private static final String CREDENTIALS_ID = "git";
    private static final String CREDENTIALS_KEY = "/org/jenkinsci/test/acceptance/docker/fixtures/GitContainer/unsafe";

    @Inject
    private DockerContainerHolder<JavaGitContainer> dockerContainer;

    /**
     * Verifies that static analysis results are correctly shown when the job is part of a folder.
     */
    @Test
    public void shouldRunInFolder() {
        Folder folder = jenkins.jobs.create(Folder.class, "singleSummary");

        FreeStyleJob job = folder.getJobs().create(FreeStyleJob.class);
        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_01");
        job.addPublisher(ReferenceFinder.class);

        addAllRecorders(job);
        job.save();

        buildJob(job);

        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = buildJob(job);

        verifyPmd(build);
        verifyFindBugs(build);
        verifyCheckStyle(build);
        verifyCpd(build);
    }

    /**
     * Tests the build overview page by running two builds that aggregate the three different tools into a single
     * result. Checks the contents of the result summary.
     */
    @Test
    public void shouldAggregateToolsIntoSingleResult() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_01");
        IssuesRecorder recorder = addAllRecorders(job);
        recorder.setEnabledForAggregation(true);
        recorder.addQualityGateConfiguration(4, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE);
        recorder.addQualityGateConfiguration(3, QualityGateType.NEW, QualityGateCriticality.FAILURE);
        recorder.setIgnoreQualityGate(true);

        job.save();

        Build referenceBuild = buildJob(job).shouldBeUnstable();
        referenceBuild.open();

        AnalysisSummary referenceSummary = new AnalysisSummary(referenceBuild, ANALYSIS_ID);
        assertThat(referenceSummary)
                .hasTitleText("Static Analysis: 4 warnings (from 4 analyses)")
                .hasTools("FindBugs (0)", "CPD (0)", "CheckStyle (1)", "PMD (3)")
                .hasNewSize(0)
                .hasFixedSize(0)
                .hasReferenceBuild(0)
                .hasQualityGateResult(QualityGateResult.UNSTABLE);

        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = buildJob(job);

        build.open();

        AnalysisSummary analysisSummary = new AnalysisSummary(build, ANALYSIS_ID);
        assertThat(analysisSummary)
                .hasTitleText("Static Analysis: 25 warnings (from 4 analyses)")
                .hasTools("FindBugs (0)", "CPD (20)", "CheckStyle (3)", "PMD (2)")
                .hasNewSize(23)
                .hasFixedSize(2)
                .hasReferenceBuild(1)
                .hasQualityGateResult(QualityGateResult.FAILED);

        AnalysisResult result = analysisSummary.openOverallResult();
        assertThat(result).hasActiveTab(Tab.TOOLS).hasTotal(25)
                .hasOnlyAvailableTabs(Tab.TOOLS, Tab.PACKAGES, Tab.FILES, Tab.CATEGORIES, Tab.TYPES, Tab.ISSUES);
    }

    /**
     * Test to check that the issue filter can be configured and is applied.
     */
    @Test
    public void shouldFilterIssuesByIncludeAndExcludeFilters() {
        FreeStyleJob job = createFreeStyleJob("issue_filter/checkstyle-result.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle").setPattern("**/checkstyle-report.xml");
            recorder.setEnabledForFailure(true);
            recorder.addIssueFilter("Exclude categories", "Checks");
            recorder.addIssueFilter("Include types", "JavadocMethodCheck");
        });

        job.save();

        Build build = buildJob(job);

        assertThat(build.getConsole()).contains(
                "Applying 2 filters on the set of 4 issues (3 issues have been removed, 1 issues will be published)");

        AnalysisResult resultPage = new AnalysisResult(build, "checkstyle");
        resultPage.open();

        IssuesTable issuesTable = resultPage.openIssuesTable();
        assertThat(issuesTable.getSize()).isEqualTo(1);
    }

    /**
     * Creates and builds a maven job and verifies that all warnings are shown in the summary and details views.
     */
    @Test
    @WithPlugins("maven-plugin")
    @SuppressWarnings("SystemOut")
    public void shouldShowMavenWarningsInMavenProject() {
        MavenModuleSet job = createMavenProject();
        copyResourceFilesToWorkspace(job, SOURCE_VIEW_FOLDER + "pom.xml");

        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setToolWithPattern(MAVEN_TOOL, "");
        recorder.setEnabledForFailure(true);
        recorder.addIssueFilter("Include types", ".*:.*");
        job.save();

        Build build = buildJob(job).shouldSucceed();

        System.out.println("-------------- Console Log ----------------");
        System.out.println(build.getConsole());
        System.out.println("-------------------------------------------");

        build.open();

        AnalysisSummary summary = new AnalysisSummary(build, MAVEN_ID);
        assertThat(summary)
                .hasTitleText("Maven: 3 warnings")
                .hasNewSize(0)
                .hasFixedSize(0)
                .hasReferenceBuild(0);

        AnalysisResult mavenDetails = summary.openOverallResult();
        assertThat(mavenDetails).hasActiveTab(Tab.TYPES)
                .hasTotal(3)
                .hasOnlyAvailableTabs(Tab.TYPES, Tab.ISSUES);

        IssuesTable issuesTable = mavenDetails.openIssuesTable();

        IssuesTableRow firstRow = issuesTable.getRow(0);
        ConsoleLogView sourceView = firstRow.openConsoleLog();
        assertThat(sourceView).hasTitle("Console Output (lines 23-43)")
                .hasHighlightedText("[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!");
    }

    /**
     * Verifies that warnings can be parsed on an agent as well.
     */
    @Test
    @WithDocker
    @WithPlugins("ssh-slaves")
    @WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {CREDENTIALS_ID, CREDENTIALS_KEY})
    @Ignore("Ignore docker based tests right now")
    public void shouldParseWarningsOnAgent() {
        DumbSlave dockerAgent = createDockerAgent();
        FreeStyleJob job = createFreeStyleJobForDockerAgent(dockerAgent, "issue_filter/checkstyle-result.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle", "**/checkstyle-report.xml"));
        job.save();

        Build build = buildJob(job);
        build.open();

        AnalysisSummary summary = new AnalysisSummary(build, "checkstyle");
        assertThat(summary)
                .hasTitleText("CheckStyle: 4 warnings")
                .hasNewSize(0)
                .hasFixedSize(0)
                .hasReferenceBuild(0);

        getDockerContainer().close();
    }

    private FreeStyleJob createFreeStyleJobForDockerAgent(final Slave dockerAgent, final String... resourcesToCopy) {
        FreeStyleJob job = createFreeStyleJob(resourcesToCopy);
        job.configure();
        job.setLabelExpression(dockerAgent.getName());
        return job;
    }

    /**
     * Returns a docker container that can be used to host git repositories and which can be used as build agent. If the
     * container is used as agent and git server, then you need to use the file protocol to access the git repository
     * within Jenkins.
     *
     * @return the container
     */
    private DockerContainer getDockerContainer() {
        return dockerContainer.get();
    }

    /**
     * Creates an agent in a Docker container.
     *
     * @return the new agent ready for new builds
     */
    @SuppressWarnings("PMD.CloseResource")
    private DumbSlave createDockerAgent() {
        DumbSlave agent = jenkins.slaves.create(DumbSlave.class);

        agent.setExecutors(1);
        agent.remoteFS.set("/tmp/");
        SshSlaveLauncher launcher = agent.setLauncher(SshSlaveLauncher.class);

        DockerContainer container = getDockerContainer();
        launcher.host.set(container.ipBound(22));
        launcher.port(container.port(22));
        launcher.setSshHostKeyVerificationStrategy(SshSlaveLauncher.NonVerifyingKeyVerificationStrategy.class);
        launcher.selectCredentials(CREDENTIALS_ID);

        agent.save();

        agent.waitUntilOnline();

        assertThat(agent.isOnline()).isTrue();

        return agent;
    }
}

