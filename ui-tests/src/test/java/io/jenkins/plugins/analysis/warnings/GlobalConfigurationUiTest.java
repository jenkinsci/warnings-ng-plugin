package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

import io.jenkins.plugins.analysis.warnings.AnalysisResult.Tab;
import io.jenkins.plugins.analysis.warnings.AnalysisSummary.InfoType;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

@WithPlugins("warnings-ng")
public class GlobalConfigurationUiTest extends AbstractUiTest {

    private static final String GCC_ID = "gcc";

    @Test
    public void shouldRunJobWithDifferentSourceCodeDirectory() throws IOException, URISyntaxException {
        FreeStyleJob job = createFreeStyleJob();
        addRecorder(job);
        job.save();

        // set global settings
        GlobalWarningsSettings settings = new GlobalWarningsSettings(jenkins);
        settings.configure();
        String homeDir = settings.getHomeDirectory();
        String jobDir = homeDir + File.separator + "jobs" + File.separator + job.name;
        settings.enterSourceDirectoryPath(jobDir);
        settings.save();

        // create dynamically built file in target workspace
        String content = String.format("%s/config.xml:451: warning: foo defined but not used%n", jobDir);

        Path workspacePath = Paths.get(homeDir).resolve("workspace");
        Files.createDirectory(workspacePath);
        workspacePath = workspacePath.resolve(job.name);
        Files.createDirectory(workspacePath);

        File newFile = workspacePath.resolve("gcc.log").toFile();
        boolean newFile1 = newFile.createNewFile();
        FileWriter writer = new FileWriter(newFile);
        writer.write(content);
        writer.flush();
        writer.close();

        // start building and verifying result
        Build build = buildJob(job);

        verifyGcc(build);
    }

    private IssuesRecorder addRecorder(final FreeStyleJob job) {
        return job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("GNU C Compiler (gcc)", gcc -> gcc.setPattern("**/gcc.log"));
            recorder.setEnabledForFailure(true);
            recorder.setSourceCodeEncoding("UTF-8");
        });
    }

    private void verifyGcc(final Build build) {
        build.open();
        AnalysisSummary gcc = new AnalysisSummary(build, GCC_ID);
        assertThat(gcc).isDisplayed()
                .hasTitleText("GNU C Compiler (gcc): One warning")
                .hasReferenceBuild(0)
                .hasInfoType(InfoType.INFO);

        AnalysisResult gccDetails = gcc.openOverallResult();
        assertThat(gccDetails).hasActiveTab(Tab.ISSUES);

        IssuesTableRow row = gccDetails.openIssuesTable().getRowAs(0, IssuesTableRow.class);
        assertThat(row.getFileLink()).isNotNull();
    }
}
