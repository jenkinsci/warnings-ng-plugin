package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

import io.jenkins.plugins.analysis.warnings.AnalysisResult.Tab;
import io.jenkins.plugins.analysis.warnings.AnalysisSummary.InfoType;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * UI tests for the global system configuration of the warnings plugin.
 *
 * @author Lukas Kirner
 */
@WithPlugins("warnings-ng")
@SuppressFBWarnings("BC")
public class GlobalConfigurationUiTest extends UiTest {
    private static final String GCC_ID = "gcc";

    private enum LinkType {
        SHOULD_HAVE_SOURCE_CODE_LINK,
        SHOULD_NOT_HAVE_SOURCE_CODE_LINK
    }

    /**
     * Verifies that a source code file will be copied from outside the workspace and linked in the open issues tab.
     */
    @Test
    public void shouldRunJobWithDifferentSourceCodeDirectory() throws IOException {
        String homeDir = getHomeDir();

        FreeStyleJob job = createFreeStyleJob();
        addGccRecorder(job, homeDir);
        job.save();

        createFileInWorkspace(job, homeDir);

        // The first build contains an error and no source code
        Build build = buildJob(job);
        verifyGcc(build, LinkType.SHOULD_NOT_HAVE_SOURCE_CODE_LINK);

        initGlobalSettingsForSourceDirectory(job);

        build = buildJob(job);
        verifyGcc(build, LinkType.SHOULD_HAVE_SOURCE_CODE_LINK);
    }

    private String getHomeDir() {
        GlobalWarningsSettings settings = new GlobalWarningsSettings(jenkins);
        settings.configure();
        return settings.getHomeDirectory();
    }

    private void addGccRecorder(final FreeStyleJob job, final String homeDir) {
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("GNU C Compiler (gcc)", gcc -> gcc.setPattern("**/gcc.log"));
            recorder.setEnabledForFailure(true);
            recorder.setSourceCodeEncoding("UTF-8");
            recorder.addSourceDirectory(getJobDir(homeDir, job));
        });
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

        Files.write(workspacePath.resolve("gcc.log"), content.getBytes(StandardCharsets.UTF_8));
    }

    private void initGlobalSettingsForSourceDirectory(final FreeStyleJob job) {
        GlobalWarningsSettings settings = new GlobalWarningsSettings(jenkins);
        settings.configure();
        String homeDir = settings.getHomeDirectory();
        String jobDir = getJobDir(homeDir, job);
        var prismSettings = new GlobalPrismSettings(jenkins);
        prismSettings.configure();
        prismSettings.enterSourceDirectoryPath(jobDir);
        settings.save();
    }

    private String getJobDir(final String homeDir, final FreeStyleJob job) {
        return homeDir + File.separator + "jobs" + File.separator + job.name;
    }

    private void verifyGcc(final Build build, final LinkType linkType) {
        build.open();

        AnalysisSummary gcc = new AnalysisSummary(build, GCC_ID);
        assertThat(gcc)
                .hasTitleText("GNU C Compiler (gcc): One warning")
                .hasReferenceBuild(linkType == LinkType.SHOULD_HAVE_SOURCE_CODE_LINK ? 1 : 0)
                .hasInfoType(linkType == LinkType.SHOULD_HAVE_SOURCE_CODE_LINK ? InfoType.INFO : InfoType.ERROR);

        AnalysisResult gccDetails = gcc.openOverallResult();
        assertThat(gccDetails).hasActiveTab(Tab.ISSUES).hasOnlyAvailableTabs(Tab.ISSUES);

        AbstractSeverityTableRow row = gccDetails.openIssuesTable().getRow(0);

        if (linkType == LinkType.SHOULD_HAVE_SOURCE_CODE_LINK) {
            assertThat(row.getFileLink()).isNotNull();
        }
        else {
            assertThatExceptionOfType(NoSuchElementException.class)
                    .as("Source code link should not be available")
                    .isThrownBy(row::getFileLink);
        }
    }

    /**
     * Verifies that a custom groovy script is correctly executed.
     */
    @Test
    public void shouldRunJobWithGroovyConfiguration() {
        initGlobalSettingsForGroovyParser();

        FreeStyleJob job = createFreeStyleJob("groovy_parser/" + PEP8_FILE);
        addGroovyRecorder(job);
        job.save();

        Build build = buildJob(job);

        verifyPep8(build, 0);
    }

    private void addGroovyRecorder(final FreeStyleJob job) {
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("Groovy Parser", gp -> gp.setPattern("**/*" + PEP8_FILE));
            recorder.setEnabledForFailure(true);
        });
    }

    @Override
    protected AnalysisResult verifyPep8Details(final AnalysisSummary pep8) {
        AnalysisResult pep8Details = pep8.openOverallResult();
        assertThat(pep8Details).hasActiveTab(Tab.CATEGORIES)
                .hasTotal(8)
                .hasOnlyAvailableTabs(Tab.CATEGORIES, Tab.ISSUES);
        return pep8Details;
    }
}
