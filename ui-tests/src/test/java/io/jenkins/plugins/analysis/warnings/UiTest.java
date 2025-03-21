package io.jenkins.plugins.analysis.warnings;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Container;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

import io.jenkins.plugins.analysis.warnings.AnalysisResult.Tab;
import io.jenkins.plugins.analysis.warnings.AnalysisSummary.InfoType;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;

/**
 * Base class for all UI tests. Provides several helper methods that can be used by all tests.
 */
@SuppressFBWarnings("BC")
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "PMD.CouplingBetweenObjects"})
abstract class UiTest extends AbstractJUnitTest {
    static final String WARNINGS_PLUGIN_PREFIX = "/";
    static final String CHECKSTYLE_ID = "checkstyle";
    static final String CHECKSTYLE_TOOL = "CheckStyle";
    static final String CHECK_STYLE_NAME = "++CheckStyle++";
    static final String CPD_ID = "cpd";
    static final String CPD_TOOL = "CPD";
    static final String PMD_ID = "pmd";
    static final String PMD_TOOL = "PMD";
    static final String FINDBUGS_ID = "findbugs";
    static final String FINDBUGS_TOOL = "FindBugs";
    static final String MAVEN_ID = "maven-warnings";
    static final String MAVEN_TOOL = "Maven";
    static final String ANALYSIS_ID = "analysis";
    static final String PEP8_ID = "pep8";
    static final String PEP8_TOOL = "PEP8";
    static final String PEP8_FILE = "pep8Test.txt";
    static final String JAVA_COMPILER = "Java Compiler";
    static final String JAVA_ID = "java";
    static final String ECLIPSE_COMPILER = "Eclipse ECJ";

    private static final String CPD_SOURCE_NAME = "Main.java";
    private static final String CPD_SOURCE_PATH = "/duplicate_code/Main.java";
    private static final String WARNING_LOW_PRIORITY = "Low";

    protected FreeStyleJob createFreeStyleJob(final String... resourcesToCopy) {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        for (String resource : resourcesToCopy) {
            job.copyResource(WARNINGS_PLUGIN_PREFIX + resource);
        }
        job.addPublisher(ReferenceFinder.class);
        return job;
    }

    protected StringBuilder createReportFilesStep(final WorkflowJob job, final int build) {
        String[] fileNames = {"checkstyle-report.xml", "pmd-report.xml", "findbugsXml.xml", "cpd.xml", "Main.java", "pep8Test.txt"};
        StringBuilder resourceCopySteps = new StringBuilder();
        for (String fileName : fileNames) {
            resourceCopySteps.append(job.copyResourceStep(
                    "/build_status_test/build_0" + build + "/" + fileName).replace("\\", "\\\\"));
        }
        return resourceCopySteps;
    }

    protected IssuesRecorder addAllRecorders(final FreeStyleJob job) {
        return job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle").setName(CHECK_STYLE_NAME).setPattern("**/checkstyle-report.xml");
            recorder.addTool("FindBugs");
            recorder.addTool("Registered Parser",
                    analysisModel -> analysisModel.setAnalysisModelId("PMD").setPattern("**/pmd-report.xml"));
            recorder.addTool("CPD",
                    cpd -> cpd.setHighThreshold(8).setNormalThreshold(3));
            recorder.setEnabledForFailure(true);
        });
    }

    protected Build buildJob(final Job job) {
        return job.startBuild().waitUntilFinished();
    }

    /**
     * Finds a resource with the given name and returns the content (decoded with UTF-8) as String.
     *
     * @param fileName
     *         name of the desired resource
     *
     * @return the content represented as {@link String}
     */
    protected String readFileToString(final String fileName) {
        return new String(readAllBytes(fileName), StandardCharsets.UTF_8);
    }

    /**
     * Reads all the bytes from a file. The method ensures that the file is closed when all bytes have been read or an
     * I/O error, or other runtime exception, is thrown.
     *
     * <p>
     * Note that this method is intended for simple cases where it is convenient to read all bytes into a byte array. It
     * is not intended for reading in large files.
     * </p>
     *
     * @param fileName
     *         name of the desired resource
     *
     * @return the content represented by a byte array
     */
    private byte[] readAllBytes(final String fileName) {
        try {
            return Files.readAllBytes(getPath(fileName));
        }
        catch (IOException | URISyntaxException e) {
            throw new AssertionError("Can't read resource " + fileName, e);
        }
    }

    Path getPath(final String name) throws URISyntaxException {
        URL resource = getClass().getResource(name);
        if (resource == null) {
            throw new AssertionError("Can't find resource " + name);
        }
        return Paths.get(resource.toURI());
    }

    protected void verifyCpd(final Build build) {
        build.open();

        AnalysisSummary cpd = new AnalysisSummary(build, CPD_ID);
        assertThat(cpd)
                .hasTitleText("CPD: 20 warnings")
                .hasNewSize(20)
                .hasFixedSize(0)
                .hasReferenceBuild(1)
                .hasInfoType(InfoType.INFO);

        AnalysisResult cpdDetails = cpd.openOverallResult();
        assertThat(cpdDetails).hasActiveTab(Tab.ISSUES).hasOnlyAvailableTabs(Tab.ISSUES);

        DryTable issuesTable = cpdDetails.openDryTable();
        assertThat(issuesTable.getSize()).isEqualTo(10);
        assertThat(issuesTable.getTotal()).isEqualTo(20);

        DryTableRow firstRow = issuesTable.getRow(0);
        DryTableRow secondRow = issuesTable.getRow(1);

        firstRow.toggleDetailsRow();
        assertThat(issuesTable.getSize()).isEqualTo(11);

        DryTableRow detailsRow = issuesTable.getRow(1);
        assertThat(detailsRow).hasDetails("Found duplicated code.\nfunctionOne();");

        assertThat(issuesTable.getRow(2)).isEqualTo(secondRow);

        firstRow.toggleDetailsRow();
        assertThat(issuesTable.getSize()).isEqualTo(10);
        assertThat(issuesTable.getRow(1)).isEqualTo(secondRow);

        SourceView sourceView = firstRow.openSourceCode();
        assertThat(sourceView).hasFileName(CPD_SOURCE_NAME);

        String expectedSourceCode = readFileToString(CPD_SOURCE_PATH);
        assertThat(sourceView.getSourceCode()).isEqualToIgnoringWhitespace(expectedSourceCode);

        cpdDetails.open();
        issuesTable = cpdDetails.openDryTable();
        firstRow = issuesTable.getRow(0);

        AnalysisResult lowSeverity = firstRow.clickOnSeverityLink();
        DryTable lowSeverityTable = lowSeverity.openDryTable();
        assertThat(lowSeverityTable.getSize()).isEqualTo(6);
        assertThat(lowSeverityTable.getTotal()).isEqualTo(6);

        for (int i = 0; i < 6; i++) {
            DryTableRow row = lowSeverityTable.getRow(i);
            assertThat(row).hasSeverity(WARNING_LOW_PRIORITY);
        }

        assertThat(openInfoView(build, CPD_ID))
                .hasNoErrorMessages()
                .hasInfoMessages("-> found 1 file",
                        "-> found 20 issues (skipped 0 duplicates)",
                        "-> 1 copied, 0 not in workspace, 0 not-found, 0 with I/O error",
                        "Issues delta (vs. reference build): outstanding: 0, new: 20, fixed: 0");
    }

    protected void verifyFindBugs(final Build build) {
        build.open();

        AnalysisSummary findbugs = new AnalysisSummary(build, FINDBUGS_ID);
        assertThat(findbugs)
                .hasTitleText("FindBugs: No warnings")
                .hasNewSize(0)
                .hasFixedSize(0)
                .hasReferenceBuild(1)
                .hasInfoType(InfoType.INFO)
                .hasDetails("No issues for 2 builds, i.e. since build: Success #1");

        assertThat(openInfoView(build, FINDBUGS_ID))
                .hasNoErrorMessages()
                .hasInfoMessages("-> found 1 file",
                        "-> found 0 issues (skipped 0 duplicates)",
                        "Issues delta (vs. reference build): outstanding: 0, new: 0, fixed: 0");
    }

    protected void verifyPmd(final Build build) {
        build.open();

        AnalysisSummary pmd = new AnalysisSummary(build, PMD_ID);
        assertThat(pmd)
                .hasTitleText("PMD: 2 warnings")
                .hasNewSize(0)
                .hasFixedSize(1)
                .hasReferenceBuild(1)
                .hasInfoType(InfoType.ERROR);

        AnalysisResult pmdDetails = pmd.openOverallResult();
        assertThat(pmdDetails).hasActiveTab(Tab.CATEGORIES)
                .hasTotal(2)
                .hasTotalNew(0)
                .hasOnlyAvailableTabs(Tab.CATEGORIES, Tab.TYPES, Tab.ISSUES);

        assertThat(openInfoView(build, PMD_ID))
                .hasInfoMessages("-> found 1 file",
                        "-> found 2 issues (skipped 0 duplicates)",
                        "Issues delta (vs. reference build): outstanding: 2, new: 0, fixed: 1")
                .hasErrorMessages("Can't create fingerprints for some files:");
    }

    protected void verifyCheckStyle(final Build build) {
        build.open();

        AnalysisSummary checkstyle = new AnalysisSummary(build, CHECKSTYLE_ID);
        assertThat(checkstyle)
                .hasTitleText(CHECK_STYLE_NAME + ": 3 warnings")
                .hasNewSize(3)
                .hasFixedSize(1)
                .hasReferenceBuild(1)
                .hasInfoType(InfoType.ERROR);

        AnalysisResult checkstyleDetails = checkstyle.openOverallResult();
        assertThat(checkstyleDetails).hasActiveTab(Tab.CATEGORIES)
                .hasTotal(3)
                .hasOnlyAvailableTabs(Tab.CATEGORIES, Tab.TYPES, Tab.ISSUES);

        IssuesTable issuesTable = checkstyleDetails.openIssuesTable();
        assertThat(issuesTable.getSize()).isEqualTo(3);
        assertThat(issuesTable.getTotal()).isEqualTo(3);

        IssuesTableRow tableRow = issuesTable.getRow(0);
        assertThat(tableRow).hasFileName("RemoteLauncher.java")
                .hasLineNumber(59)
                .hasCategory("Checks")
                .hasType("FinalParametersCheck")
                .hasSeverity("Error")
                .hasAge(1);

        verifyTrendCharts(checkstyleDetails);

        assertThat(openInfoView(build, CHECKSTYLE_ID))
                .hasInfoMessages("-> found 1 file",
                        "-> found 3 issues (skipped 0 duplicates)",
                        "Issues delta (vs. reference build): outstanding: 0, new: 3, fixed: 1")
                .hasErrorMessages("Can't create fingerprints for some files:");
    }

    private void verifyTrendCharts(final AnalysisResult analysisResult) {
        String severitiesTrendChart = analysisResult.getTrendChartById("severities-trend-chart");
        String toolsTrendChart = analysisResult.getTrendChartById("tools-trend-chart");
        String newVersusFixedTrendChart = analysisResult.getTrendChartById("new-versus-fixed-trend-chart");

        assertThatJson(severitiesTrendChart)
                .inPath("$.xAxis[*].data[*]")
                .isArray()
                .hasSize(2)
                .contains("#1")
                .contains("#2");

        assertThatJson(severitiesTrendChart)
                .node("series")
                .isArray()
                .hasSize(1);

        assertThatJson(severitiesTrendChart)
                .node("series[0].name").isEqualTo("Error");

        assertThatJson(severitiesTrendChart)
                .node("series[0].data").isArray().contains(1).contains(3);

        assertThatJson(toolsTrendChart)
                .inPath("$.xAxis[*].data[*]")
                .isArray()
                .hasSize(2);

        assertThatJson(toolsTrendChart)
                .node("series[0].name").isEqualTo("checkstyle");

        assertThatJson(toolsTrendChart)
                .node("series[0].data")
                .isArray()
                .contains(1)
                .contains(3);

        assertThatJson(newVersusFixedTrendChart)
                .inPath("$.xAxis[*].data[*]")
                .isArray()
                .hasSize(2)
                .contains("#1")
                .contains("#2");

        assertThatJson(newVersusFixedTrendChart)
                .and(
                        a -> a.node("series[0].name").isEqualTo("New"),
                        a -> a.node("series[0].data").isArray()
                                .contains(0)
                                .contains(3),
                        a -> a.node("series[1].name").isEqualTo("Fixed"),
                        a -> a.node("series[1].data").isArray()
                                .contains(0)
                                .contains(1)
                );
    }

    protected void verifyPep8(final Build build) {
        verifyPep8(build, 1);
    }

    protected void verifyPep8(final Build build, final int referenceBuild) {
        build.open();

        AnalysisSummary pep8 = new AnalysisSummary(build, PEP8_ID);
        assertThat(pep8)
                .hasTitleText(PEP8_TOOL + ": 8 warnings")
                .hasReferenceBuild(referenceBuild)
                .hasInfoType(InfoType.ERROR);

        AnalysisResult pep8Details = verifyPep8Details(pep8);

        pep8Details.openTab(Tab.ISSUES);
        IssuesTable issuesTable = pep8Details.openIssuesTable();
        assertThat(issuesTable.getSize()).isEqualTo(8);

        long normalIssueCount = getCountOfSeverity(issuesTable, "Normal");
        long lowIssueCount = getCountOfSeverity(issuesTable, "Low");

        assertThat(normalIssueCount).isEqualTo(6);
        assertThat(lowIssueCount).isEqualTo(2);

        assertThat(openInfoView(build, PEP8_ID))
                .hasInfoMessages("-> found 1 file",
                        "-> found 8 issues (skipped 0 duplicates)")
                .hasErrorMessages("Can't create fingerprints for some files:");

        if (referenceBuild > 0) {
            assertThat(openInfoView(build, PEP8_ID))
                    .hasInfoMessages("Issues delta (vs. reference build): outstanding: 0, new: 8, fixed: 0");
        }
    }

    private long getCountOfSeverity(final IssuesTable issuesTable, final String normal) {
        return issuesTable.getTableRows().stream()
                .map(AbstractSeverityTableRow::getSeverity)
                .filter(normal::equals).count();
    }

    protected AnalysisResult verifyPep8Details(final AnalysisSummary pep8) {
        AnalysisResult pep8Details = pep8.openOverallResult();
        assertThat(pep8Details).hasActiveTab(Tab.ISSUES)
                .hasOnlyAvailableTabs(Tab.CATEGORIES, Tab.ISSUES);
        return pep8Details;
    }

    InfoView openInfoView(final Build build, final String toolId) {
        build.open();

        return new AnalysisSummary(build, toolId).openInfoView();
    }

    protected Build buildSuccessfully(final Job job) {
        return job.startBuild().waitUntilFinished().shouldSucceed();
    }

    protected DashboardView createDashboardWithStaticAnalysisPortlet(final boolean hideCleanJobs,
            final boolean showIcons) {
        return createDashboardWithStaticAnalysisPortlet(hideCleanJobs, showIcons, jenkins);
    }

    protected DashboardView createDashboardWithStaticAnalysisPortlet(final boolean hideCleanJobs,
            final boolean showIcons, final Container container) {
        DashboardView view = createDashboardView(container);
        StaticAnalysisIssuesPerToolAndJobPortlet portlet = view.addTopPortlet(
                StaticAnalysisIssuesPerToolAndJobPortlet.class);
        portlet.setHideCleanJobs(hideCleanJobs);
        portlet.setShowIcons(showIcons);
        view.save();

        return view;
    }

    private DashboardView createDashboardView(final Container container) {
        DashboardView view = container.getViews().create(DashboardView.class);
        view.configure();
        view.matchAllJobs();
        return view;
    }

    protected void reconfigureJobWithResource(final FreeStyleJob job, final String fileName) {
        job.configure(() -> job.copyResource(WARNINGS_PLUGIN_PREFIX + fileName));
    }

    protected void copyResourceFilesToWorkspace(final Job job, final String... resources) {
        for (String file : resources) {
            job.copyResource(file);
        }
    }

    protected MavenModuleSet createMavenProject() {
        MavenInstallation.installMaven(jenkins, MavenInstallation.DEFAULT_MAVEN_ID, "3.6.3");

        return jenkins.getJobs().create(MavenModuleSet.class);
    }

    protected void initGlobalSettingsForGroovyParser() {
        GlobalWarningsSettings settings = new GlobalWarningsSettings(jenkins);
        settings.configure();
        GroovyConfiguration groovyConfiguration = settings.openGroovyConfiguration();
        groovyConfiguration.enterName(PEP8_TOOL);
        groovyConfiguration.enterId(PEP8_ID);
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
}
