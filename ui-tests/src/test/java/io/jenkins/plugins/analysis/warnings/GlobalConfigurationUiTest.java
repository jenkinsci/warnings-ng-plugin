package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private static final String PEP8_ID = "pep8-groovy";

    private static final String PEP_FILE = "pep8Test.txt";

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

        initGlobalSettingsForSourceDirectory(job);

        Build build = buildJob(job);

        verifyGcc(build);
    }

    /**
     * Verifies that a custom groovy script is correctly executed.
     */
    @Test
    public void ShouldRunJobWithGroovyConfiguration() {
        initGlobalSettingsForGroovyParser();

        FreeStyleJob job = createFreeStyleJob("groovy_parser/" + PEP_FILE);
        addGroovyRecorder(job);
        job.save();

        Build build = buildJob(job);

        verifyPep8(build);
    }

    private void initGlobalSettingsForSourceDirectory(final FreeStyleJob job) {
        GlobalWarningsSettings settings = new GlobalWarningsSettings(jenkins);
        settings.configure();
        String homeDir = settings.getHomeDirectory();
        String jobDir = getJobDir(homeDir, job);
        settings.enterSourceDirectoryPath(jobDir);
        settings.save();
    }

    private void initGlobalSettingsForGroovyParser() {
        GlobalWarningsSettings settings = new GlobalWarningsSettings(jenkins);
        settings.configure();
        GroovyConfiguration groovyConfiguration = settings.openGroovyConfiguration();
        groovyConfiguration.enterName("Pep8 Groovy Parser");
        groovyConfiguration.enterId("pep8-groovy");
        groovyConfiguration.enterRegex("(.*):(\\d+):(\\d+): (\\D\\d*) (.*)");
        groovyConfiguration.enterScript("import edu.hm.hafner.analysis.Severity\n"
                + "\n"
                + "String message = matcher.group(5)\n"
                + "String category = matcher.group(4)\n"
                + "Severity severity\n"
                + "if (category.contains(\"E\")) {\n"
                + "    severity = Severity.WARNING_NORMAL\n"
                + "}else {\n"
                + "    severity = Severity.WARNING_LOW\n"
                + "}\n"
                + "\n"
                + "return builder.setFileName(matcher.group(1))\n"
                + "    .setLineStart(Integer.parseInt(matcher.group(2)))\n"
                + "    .setColumnStart(Integer.parseInt(matcher.group(3)))\n"
                + "    .setCategory(category)\n"
                + "    .setMessage(message)\n"
                + "    .setSeverity(severity)\n"
                + "    .buildOptional()");

        groovyConfiguration.enterExampleLogMessage("optparse.py:69:11: E401 multiple imports on one line");

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

    private void addGccRecorder(final FreeStyleJob job, final String homeDir) {
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("GNU C Compiler (gcc)", gcc -> gcc.setPattern("**/gcc.log"));
            recorder.setEnabledForFailure(true);
            recorder.setSourceCodeEncoding("UTF-8");
            recorder.setSourceDirectory(getJobDir(homeDir, job));
        });
    }

    private void addGroovyRecorder(final FreeStyleJob job) {
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("Groovy Parser", gp -> gp.setPattern("**/*" + PEP_FILE));
            recorder.setEnabledForFailure(true);
            ;
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
        assertThat(gccDetails).hasActiveTab(Tab.ISSUES)
                .hasOnlyAvailableTabs(Tab.ISSUES);

        IssuesTableRow row = gccDetails.openIssuesTable().getRowAs(0, IssuesTableRow.class);
        assertThat(row.getFileLink()).isNotNull();
    }

    private void verifyPep8(final Build build) {
        build.open();
        AnalysisSummary pep8 = new AnalysisSummary(build, PEP8_ID);
        assertThat(pep8).isDisplayed()
                .hasTitleText("Pep8 Groovy Parser: 8 warnings")
                .hasReferenceBuild(0)
                .hasInfoType(InfoType.ERROR);

        AnalysisResult pep8details = pep8.openOverallResult();
        assertThat(pep8details).hasActiveTab(Tab.CATEGORIES)
                .hasTotal(8)
                .hasOnlyAvailableTabs(Tab.CATEGORIES, Tab.ISSUES);

        pep8details.openTab(Tab.ISSUES);
        IssuesTable issuesTable = pep8details.openIssuesTable();
        assertThat(issuesTable).hasSize(8);

        long NormalIssueCount = issuesTable.getTableRows().stream()
                .map(row -> row.getAs(IssuesTableRow.class).getSeverity())
                .filter(severity -> severity.equals("Normal")).count();

        long LowIssueCount = issuesTable.getTableRows().stream()
                .map(row -> row.getAs(IssuesTableRow.class).getSeverity())
                .filter(severity -> severity.equals("Low")).count();

        assertThat(NormalIssueCount).isEqualTo(6);
        assertThat(LowIssueCount).isEqualTo(2);
    }
}
