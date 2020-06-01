package io.jenkins.plugins.analysis.warnings;

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
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;

import io.jenkins.plugins.analysis.warnings.AnalysisResult.Tab;
import io.jenkins.plugins.analysis.warnings.AnalysisSummary.InfoType;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * Base class for all UI tests. Provides several helper methods that can be used by all tests.
 */
abstract class UiTest extends AbstractJUnitTest {
    static final String WARNINGS_PLUGIN_PREFIX = "/";
    static final String CHECKSTYLE_ID = "checkstyle";
    static final String CPD_ID = "cpd";
    static final String PMD_ID = "pmd";
    static final String FINDBUGS_ID = "findbugs";
    static final String MAVEN_ID = "maven-warnings";
    static final String ANALYSIS_ID = "analysis";

    private static final String CPD_SOURCE_NAME = "Main.java";
    private static final String CPD_SOURCE_PATH = "/duplicate_code/Main.java";
    private static final String WARNING_LOW_PRIORITY = "Low";

    protected FreeStyleJob createFreeStyleJob(final String... resourcesToCopy) {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        ScrollerUtil.hideScrollerTabBar(driver);
        for (String resource : resourcesToCopy) {
            job.copyResource(WARNINGS_PLUGIN_PREFIX + resource);
        }
        return job;
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
     * Reads the contents of the desired resource. The rules for searching resources associated with this test class are
     * implemented by the defining {@linkplain ClassLoader class loader} of this test class.  This method delegates to
     * this object's class loader.  If this object was loaded by the bootstrap class loader, the method delegates to
     * {@link ClassLoader#getSystemResource}.
     * <p>
     * Before delegation, an absolute resource name is constructed from the given resource name using this algorithm:
     * </p>
     * <ul>
     * <li> If the {@code name} begins with a {@code '/'} (<tt>'&#92;u002f'</tt>), then the absolute name of the
     * resource is the portion of the {@code name} following the {@code '/'}.</li>
     * <li> Otherwise, the absolute name is of the following form:
     * <blockquote> {@code modified_package_name/name} </blockquote>
     * <p> Where the {@code modified_package_name} is the package name of this object with {@code '/'}
     * substituted for {@code '.'} (<tt>'&#92;u002e'</tt>).</li>
     * </ul>
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
        assertThat(cpd).isDisplayed()
                .hasTitleText("CPD: 20 warnings")
                .hasNewSize(20)
                .hasFixedSize(0)
                .hasReferenceBuild(1)
                .hasInfoType(InfoType.INFO);

        AnalysisResult cpdDetails = cpd.openOverallResult();
        assertThat(cpdDetails).hasActiveTab(Tab.ISSUES).hasOnlyAvailableTabs(Tab.ISSUES);

        IssuesTable issuesTable = cpdDetails.openIssuesTable();
        assertThat(issuesTable).hasSize(10).hasTotal(20);

        DryIssuesTableRow firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);
        DryIssuesTableRow secondRow = issuesTable.getRowAs(1, DryIssuesTableRow.class);

        firstRow.toggleDetailsRow();
        assertThat(issuesTable).hasSize(11);

        DetailsTableRow detailsRow = issuesTable.getRowAs(1, DetailsTableRow.class);
        assertThat(detailsRow).hasDetails("Found duplicated code.\nfunctionOne();");

        assertThat(issuesTable.getRowAs(2, DryIssuesTableRow.class)).isEqualTo(secondRow);

        firstRow.toggleDetailsRow();
        assertThat(issuesTable).hasSize(10);
        assertThat(issuesTable.getRowAs(1, DryIssuesTableRow.class)).isEqualTo(secondRow);

        SourceView sourceView = firstRow.openSourceCode();
        assertThat(sourceView).hasFileName(CPD_SOURCE_NAME);

        String expectedSourceCode = readFileToString(CPD_SOURCE_PATH);
        assertThat(sourceView.getSourceCode()).isEqualToIgnoringWhitespace(expectedSourceCode);

        cpdDetails.open();
        issuesTable = cpdDetails.openIssuesTable();
        firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);

        AnalysisResult lowSeverity = firstRow.clickOnSeverityLink();
        IssuesTable lowSeverityTable = lowSeverity.openIssuesTable();
        assertThat(lowSeverityTable).hasSize(6).hasTotal(6);

        for (int i = 0; i < 6; i++) {
            DryIssuesTableRow row = lowSeverityTable.getRowAs(i, DryIssuesTableRow.class);
            assertThat(row).hasSeverity(WARNING_LOW_PRIORITY);
        }

        build.open();
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
        assertThat(findbugs).isDisplayed()
                .hasTitleText("FindBugs: No warnings")
                .hasNewSize(0)
                .hasFixedSize(0)
                .hasReferenceBuild(1)
                .hasInfoType(InfoType.INFO)
                .hasDetails("No warnings for 2 builds, i.e. since build 1");

        build.open();
        assertThat(openInfoView(build, FINDBUGS_ID))
                .hasNoErrorMessages()
                .hasInfoMessages("-> found 1 file",
                        "-> found 0 issues (skipped 0 duplicates)",
                        "Issues delta (vs. reference build): outstanding: 0, new: 0, fixed: 0");
    }

    protected void verifyPmd(final Build build) {
        build.open();
        AnalysisSummary pmd = new AnalysisSummary(build, PMD_ID);
        assertThat(pmd).isDisplayed()
                .hasTitleText("PMD: 2 warnings")
                .hasNewSize(0)
                .hasFixedSize(1)
                .hasReferenceBuild(1)
                .hasInfoType(InfoType.ERROR);

        AnalysisResult pmdDetails = pmd.openOverallResult();
        assertThat(pmdDetails).hasActiveTab(Tab.CATEGORIES)
                .hasTotal(2)
                .hasOnlyAvailableTabs(Tab.CATEGORIES, Tab.TYPES, Tab.ISSUES);

        build.open();
        assertThat(openInfoView(build, PMD_ID))
                .hasInfoMessages("-> found 1 file",
                        "-> found 2 issues (skipped 0 duplicates)",
                        "Issues delta (vs. reference build): outstanding: 2, new: 0, fixed: 1")
                .hasErrorMessages("Can't create fingerprints for some files:");
    }

    protected void verifyCheckStyle(final Build build) {
        build.open();
        AnalysisSummary checkstyle = new AnalysisSummary(build, CHECKSTYLE_ID);
        assertThat(checkstyle).isDisplayed()
                .hasTitleText("CheckStyle: 3 warnings")
                .hasNewSize(3)
                .hasFixedSize(1)
                .hasReferenceBuild(1)
                .hasInfoType(InfoType.ERROR);

        AnalysisResult checkstyleDetails = checkstyle.openOverallResult();
        assertThat(checkstyleDetails).hasActiveTab(Tab.CATEGORIES)
                .hasTotal(3)
                .hasOnlyAvailableTabs(Tab.CATEGORIES, Tab.TYPES, Tab.ISSUES);

        IssuesTable issuesTable = checkstyleDetails.openIssuesTable();
        assertThat(issuesTable).hasSize(3).hasTotal(3);

        DefaultIssuesTableRow tableRow = issuesTable.getRowAs(0, DefaultIssuesTableRow.class);
        assertThat(tableRow).hasFileName("RemoteLauncher.java")
                .hasLineNumber(59)
                .hasCategory("Checks")
                .hasType("FinalParametersCheck")
                .hasSeverity("Error")
                .hasAge(1);

        build.open();
        assertThat(openInfoView(build, CHECKSTYLE_ID))
                .hasInfoMessages("-> found 1 file",
                        "-> found 3 issues (skipped 0 duplicates)",
                        "Issues delta (vs. reference build): outstanding: 0, new: 3, fixed: 1")
                .hasErrorMessages("Can't create fingerprints for some files:");
    }

    InfoView openInfoView(final Build build, final String toolId) {
        return new AnalysisSummary(build, toolId).openInfoView();
    }

    protected Build shouldBuildSuccessfully(final Job job) {
        return job.startBuild().waitUntilFinished().shouldSucceed();
    }

    protected DashboardView createDashboardWithStaticAnalysisPortlet(final boolean hideCleanJobs, final boolean showIcons) {
        DashboardView view = createDashboardView();
        StaticAnalysisIssuesPerToolAndJobPortlet portlet = view.addTopPortlet(StaticAnalysisIssuesPerToolAndJobPortlet.class);
        if (hideCleanJobs) {
            portlet.toggleHideCleanJobs();
        }
        if (showIcons) {
            portlet.toggleShowIcons();
        }
        view.save();

        return view;
    }

    private DashboardView createDashboardView() {
        DashboardView view = jenkins.views.create(DashboardView.class);
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
}
