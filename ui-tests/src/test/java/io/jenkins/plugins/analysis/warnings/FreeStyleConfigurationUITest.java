package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.google.inject.Inject;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaGitContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithCredentials;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.ssh_slaves.SshSlaveLauncher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
//import io.jenkins.plugins.analysis.core.util.TrendChartType;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

import io.jenkins.plugins.analysis.warnings.AnalysisResult.Tab;
import io.jenkins.plugins.analysis.warnings.AnalysisSummary.InfoType;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder.QualityGateBuildResult;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder.QualityGateType;


/**
 * Acceptance tests for the Warnings Next Generation Plugin.

 */
@WithPlugins("warnings-ng")
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "PMD.SystemPrintln", "PMD.ExcessiveImports"})
public class FreeStyleConfigurationUITest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/";

    private static final String CHECKSTYLE_ID = "checkstyle";
    private static final String ANALYSIS_ID = "analysis";
    private static final String CPD_ID = "cpd";
    private static final String PMD_ID = "pmd";
    private static final String FINDBUGS_ID = "findbugs";
    private static final String MAVEN_ID = "maven-warnings";

    private static final String WARNING_LOW_PRIORITY = "Low";

    private static final String SOURCE_VIEW_FOLDER = WARNINGS_PLUGIN_PREFIX + "source-view/";

    private static final String CPD_SOURCE_NAME = "Main.java";
    private static final String CPD_SOURCE_PATH = "duplicate_code/Main.java";

    private static final String PATTERN = "**/*.txt";
    private static final String ENCODING = "UTF-8";
    private static final String REFERENCE = "reference";
    private static final String SOURCE_DIRECTORY = "relative";

    @Test
    public void shouldSetPropertiesInJobConfiguration() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);


        IssuesRecorder issuesRecorder = job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("Eclipse ECJ");
        });

        issuesRecorder.setSourceCodeEncoding(ENCODING);
        issuesRecorder.setSourceDirectory(SOURCE_DIRECTORY);
        issuesRecorder.setAggregatingResults(true);
        // issuesRecorder.setTrendChartType(TrendChartType.TOOLS_ONLY);
        issuesRecorder.setBlameDisabled(true);
        issuesRecorder.setForensicsDisabled(true);
        issuesRecorder.setEnabledForFailure(true);
        issuesRecorder.setIgnoreQualityGate(true);
        issuesRecorder.setIgnoreFailedBuilds(true);
        issuesRecorder.setFailOnError(true);
        issuesRecorder.setReferenceJobField(REFERENCE);
        // .setHealthReport(1, 9, Severity.WARNING_HIGH)
        issuesRecorder.setReportFilePattern(PATTERN);



        job.save();
        job.configure();
        issuesRecorder.openAdvancedOptions();
        //assertThat(issuesRecorder.getEnabledForFailure()).isEqualTo("on");

        /*
         find(by.name("_.aggregatingResults")).click();
        find(by.name("_.enabledForFailure")).click();
        job.save();

        job.configure();
        find(by.id("yui-gen21-button")).click();
        find(by.name("_.aggregatingResults")).isSelected();

        Build build =job.startBuild().waitUntilFinished();
        //console is on screen
        build.open();



        AnalysisSummary eclipse = new AnalysisSummary(build, "eclipse");
        assertThat(eclipse.openInfoView()).hasInfoMessages("-> found 0 issues (skipped 0 duplicates)");
**/

    }
}


