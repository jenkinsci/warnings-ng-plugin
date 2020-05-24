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
        String homeDir = getHomeDir();

        FreeStyleJob job = initJob(homeDir);

        // create dynamically built file in target workspace
        createFileInWorkspace(job, homeDir);

        // set global settings
        initGlobalSettings(job);

        // start building and verifying result
        Build build = buildJob(job);

        verifyGcc(build);
    }

    private FreeStyleJob initJob(final String homeDir) {
        FreeStyleJob job = createFreeStyleJob();
        addRecorder(job, homeDir);
        job.save();
        return job;
    }

    private void initGlobalSettings(final FreeStyleJob job) {
        GlobalWarningsSettings settings = new GlobalWarningsSettings(jenkins);
        settings.configure();
        String homeDir = settings.getHomeDirectory();
        String jobDir = getJobDir(homeDir, job);
        settings.enterSourceDirectoryPath(jobDir);
        settings.save();
    }

    private String getHomeDir() {
        GlobalWarningsSettings settings = new GlobalWarningsSettings(jenkins);
        settings.configure();
        return settings.getHomeDirectory();
    }

    private void createFileInWorkspace(final FreeStyleJob job, final String homeDir) throws IOException {
        String content = String.format("%s/config.xml:451: warning: foo defined but not used%n",
                getJobDir(homeDir, job));

        Path workspacePath = Paths.get(homeDir).resolve("workspace");
        if (Files.notExists(workspacePath)) {
            Files.createDirectory(workspacePath);
        }
        workspacePath = workspacePath.resolve(job.name);
        if (Files.notExists(workspacePath)) {
            Files.createDirectory(workspacePath);
        }

        File newFile = workspacePath.resolve("gcc.log").toFile();
        boolean newFile1 = newFile.createNewFile();
        if (!newFile1) {
            return;
        }
        FileWriter writer = new FileWriter(newFile);
        writer.write(content);
        writer.flush();
        writer.close();
    }

    private String getJobDir(final String homeDir, final FreeStyleJob job) {
        return homeDir + File.separator + "jobs" + File.separator + job.name;
    }

    private IssuesRecorder addRecorder(final FreeStyleJob job, final String homeDir) {
        return job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("GNU C Compiler (gcc)", gcc -> gcc.setPattern("**/gcc.log"));
            recorder.setEnabledForFailure(true);
            recorder.setSourceCodeEncoding("UTF-8");
            recorder.setSourceDirectory(getJobDir(homeDir, job));
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
