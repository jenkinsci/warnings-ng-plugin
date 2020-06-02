package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import com.google.inject.Inject;

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
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Slave;

import io.jenkins.plugins.analysis.warnings.AnalysisResult.Tab;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder.QualityGateBuildResult;
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
     * Tests the build overview page by running two builds that aggregate the three different tools into a single
     * result. Checks the contents of the result summary.
     */
    @Test
    public void shouldAggregateToolsIntoSingleResult() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_01");
        IssuesRecorder recorder = addAllRecorders(job);
        recorder.setEnabledForAggregation(true);
        recorder.addQualityGateConfiguration(4, QualityGateType.TOTAL, QualityGateBuildResult.UNSTABLE);
        recorder.addQualityGateConfiguration(3, QualityGateType.NEW, QualityGateBuildResult.FAILED);
        recorder.setIgnoreQualityGate(true);

        job.save();

        Build referenceBuild = buildJob(job).shouldBeUnstable();
        referenceBuild.open();

        assertThat(new AnalysisSummary(referenceBuild, CHECKSTYLE_ID)).isNotDisplayed();
        assertThat(new AnalysisSummary(referenceBuild, PMD_ID)).isNotDisplayed();
        assertThat(new AnalysisSummary(referenceBuild, FINDBUGS_ID)).isNotDisplayed();

        AnalysisSummary referenceSummary = new AnalysisSummary(referenceBuild, ANALYSIS_ID);
        assertThat(referenceSummary).isDisplayed()
                .hasTitleText("Static Analysis: 4 warnings")
                .hasAggregation("FindBugs, CPD, CheckStyle, PMD")
                .hasNewSize(0)
                .hasFixedSize(0)
                .hasReferenceBuild(0);

        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = buildJob(job);

        build.open();

        AnalysisSummary analysisSummary = new AnalysisSummary(build, ANALYSIS_ID);
        assertThat(analysisSummary).isDisplayed()
                .hasTitleText("Static Analysis: 25 warnings")
                .hasAggregation("FindBugs, CPD, CheckStyle, PMD")
                .hasNewSize(23)
                .hasFixedSize(2)
                .hasReferenceBuild(1);

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
            recorder.setTool("CheckStyle");
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
        assertThat(issuesTable).hasSize(1);
    }

    /**
     * Creates and builds a maven job and verifies that all warnings are shown in the summary and details views.
     */
    @Test
    @WithPlugins("maven-plugin")
    public void shouldShowMavenWarningsInMavenProject() {
        MavenModuleSet job = createMavenProject();
        copyResourceFilesToWorkspace(job, SOURCE_VIEW_FOLDER + "pom.xml");

        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setToolWithPattern("Maven", "");
        recorder.setEnabledForFailure(true);

        job.save();

        Build build = buildJob(job).shouldSucceed();

        System.out.println("-------------- Console Log ----------------");
        System.out.println(build.getConsole());
        System.out.println("-------------------------------------------");

        build.open();

        AnalysisSummary summary = new AnalysisSummary(build, MAVEN_ID);
        assertThat(summary).isDisplayed()
                .hasTitleText("Maven: 4 warnings")
                .hasNewSize(0)
                .hasFixedSize(0)
                .hasReferenceBuild(0);

        AnalysisResult mavenDetails = summary.openOverallResult();
        assertThat(mavenDetails).hasActiveTab(Tab.MODULES)
                .hasTotal(4)
                .hasOnlyAvailableTabs(Tab.MODULES, Tab.TYPES, Tab.ISSUES);

        IssuesTable issuesTable = mavenDetails.openIssuesTable();

        DefaultIssuesTableRow firstRow = issuesTable.getRowAs(0, DefaultIssuesTableRow.class);
        ConsoleLogView sourceView = firstRow.openConsoleLog();
        assertThat(sourceView).hasTitle("Console Details")
                .hasHighlightedText("[WARNING]\n"
                        + "[WARNING] Some problems were encountered while building the effective model for edu.hm.hafner.irrelevant.groupId:random-artifactId:jar:1.0\n"
                        + "[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-compiler-plugin is missing. @ line 13, column 15\n"
                        + "[WARNING]\n"
                        + "[WARNING] It is highly recommended to fix these problems because they threaten the stability of your build.\n"
                        + "[WARNING]\n"
                        + "[WARNING] For this reason, future Maven versions might no longer support building such malformed projects.\n"
                        + "[WARNING]");
    }

    /**
     * Verifies that warnings can be parsed on a agent as well.
     */
    @Test
    @WithDocker
    @WithPlugins("ssh-slaves")
    @WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {CREDENTIALS_ID, CREDENTIALS_KEY})
    public void shouldParseWarningsOnAgent() {
        DumbSlave dockerAgent = createDockerAgent();
        FreeStyleJob job = createFreeStyleJobForDockerAgent(dockerAgent, "issue_filter/checkstyle-result.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle", "**/checkstyle-result.xml"));
        job.save();

        Build build = buildJob(job);
        build.open();

        AnalysisSummary summary = new AnalysisSummary(build, "checkstyle");
        assertThat(summary).isDisplayed()
                .hasTitleText("CheckStyle: 4 warnings")
                .hasNewSize(0)
                .hasFixedSize(0)
                .hasReferenceBuild(0);
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

