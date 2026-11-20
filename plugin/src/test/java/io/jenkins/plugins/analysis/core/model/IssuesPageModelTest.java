package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.IssuesPageModel.TableModelFactory;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;
import io.jenkins.plugins.analysis.core.model.ToolsTableModel.ToolRow;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableColumn.ColumnBuilder;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.forensics.util.CommitDecorator.NullDecorator;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * Tests the class {@link IssuesPageModel}.
 *
 * @author Ullrich Hafner
 */
class IssuesPageModelTest extends AbstractDetailsModelTest {
    private static final String CHECKSTYLE = "checkstyle";
    private static final String PMD = "pmd";

    /**
     * Verifies that the page provides the basic properties and all tools with issues.
     */
    @Test
    void shouldProvideToolsWithIssues() {
        var checkstyle = createAction(CHECKSTYLE, createReport(createIssue(1)), new Report());
        var pmd = createAction(PMD, createReport(createIssue(2)), new Report());
        var spotbugs = createAction("spotbugs", new Report(), new Report());
        Run<?, ?> run = mock(Run.class);

        var model = new IssuesPageModel(run, List.of(checkstyle, spotbugs, pmd));

        assertThat(model).hasDisplayName("Issues").hasUrlName("table").hasAllToolsId(TrendModel.ALL_TOOLS_ID);
        assertThat(model.getObject()).isSameAs(run);
        assertThat(model.getTab().getObject()).isSameAs(run);
        assertThat(model.getTools()).containsExactly(checkstyle, pmd);
        assertThat(model.hasSeveralTools()).isTrue();
        assertThat(model.hasIssues()).isTrue();
        assertThat(model.hasFixedIssues()).isFalse();

        var single = new IssuesPageModel(run, List.of(checkstyle, spotbugs));

        assertThat(single.getTools()).containsExactly(checkstyle);
        assertThat(single.hasSeveralTools()).isFalse();
        assertThat(single.hasIssues()).isTrue();

        var empty = new IssuesPageModel(run, List.of(spotbugs));

        assertThat(empty.getTools()).isEmpty();
        assertThat(empty.hasIssues()).isFalse();
    }

    /**
     * Verifies that the tools of the page are linked to the page, and all other tools to the log messages.
     */
    @Test
    void shouldLinkToolsOfPageToPage() {
        var checkstyle = createAction(CHECKSTYLE, createReport(createIssue(1)), new Report());
        var cpd = createActionWithCustomTable("cpd", createReport(createIssue(2)), mock(DetailsTableModel.class));
        var spotbugs = createAction("spotbugs", new Report(), new Report());
        var custom = createAction("my id", createReport(createIssue(3)), new Report());

        var model = new IssuesPageModel(mock(Run.class), List.of(checkstyle, cpd, spotbugs, custom));

        assertThat(model.getUrlOf(checkstyle)).isEqualTo("table#tool=checkstyle");
        assertThat(model.getUrlOf(custom)).isEqualTo("table#tool=my%20id");
        assertThat(model.getUrlOf(cpd)).isEqualTo("table#tool=cpd");
        assertThat(model.getUrlOf(spotbugs)).isEqualTo("log#tool=spotbugs");
    }

    /**
     * Verifies that a tool with a custom issues table is part of the page, and that the page provides the custom
     * table of this tool in addition to the table of all tools.
     */
    @Test
    void shouldProvideCustomTableOfTool() {
        var issue = createIssue(2);
        var checkstyle = createAction(CHECKSTYLE, createReport(createIssue(1)), new Report());
        DetailsTableModel customModel = mock(DetailsTableModel.class);
        var customColumn = new ColumnBuilder()
                .withHeaderLabel("Custom")
                .withDataPropertyKey("custom")
                .build();
        DetailsTableModel.TableRow customRow = mock(DetailsTableModel.TableRow.class);
        when(customModel.getColumns()).thenReturn(List.of(customColumn));
        when(customModel.getRow(issue)).thenReturn(customRow);
        var cpd = createActionWithCustomTable("cpd", createReport(issue), customModel);
        var pmd = createAction(PMD, createReport(createIssue(3)), new Report());

        var model = new IssuesPageModel(mock(Run.class), List.of(checkstyle, cpd, pmd));

        assertThat(model.getTools()).containsExactly(checkstyle, cpd, pmd);
        assertThat(model.getCustomTableIds()).containsExactly("issues-tool-1");
        assertThat(model.getIssuesTableId(checkstyle)).isEqualTo("issues");
        assertThat(model.getIssuesTableId(cpd)).isEqualTo("issues-tool-1");
        assertThat(model.getIssuesTableId(pmd)).isEqualTo("issues");

        var table = model.getTableModel("issues-tool-1");

        assertThat(table.getId()).isEqualTo("issues-tool-1");
        assertThat(table.getColumns())
                .extracting(TableColumn::getHeaderLabel)
                .containsExactly("Custom", "Hiddentool", "Hiddenbaseline");
        assertThat(table.getRows()).hasSize(1).first().isInstanceOfSatisfying(ToolRow.class, row -> {
            assertThat(row.getRow()).isSameAs(customRow);
            assertThat(row.getTool()).isEqualTo("cpd");
            assertThat(row.getBaseline()).isEqualTo("outstanding");
        });
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> model.getTableModel("issues-tool-0"));
    }

    /**
     * Verifies that the default issues table is used in the table of all tools if a tool provides a custom table.
     */
    @Test
    void shouldUseDefaultIssuesTableForAllTools() {
        var checkstyle = createAction(CHECKSTYLE, createReport(createIssue(1)), new Report());
        DetailsTableModel customModel = mock(DetailsTableModel.class);
        var cpd = createActionWithCustomTable("cpd", createReport(createIssue(2)), customModel);
        var factory = new TableModelFactory();

        assertThat(factory.hasCustomIssuesTable(checkstyle)).isFalse();
        assertThat(factory.createCustomIssuesModel(checkstyle)).isInstanceOf(IssuesModel.class);
        assertThat(factory.createIssuesModel(checkstyle)).isSameAs(factory.createCustomIssuesModel(checkstyle));

        assertThat(factory.hasCustomIssuesTable(cpd)).isTrue();
        assertThat(factory.createCustomIssuesModel(cpd)).isSameAs(customModel);
        assertThat(factory.createIssuesModel(cpd)).isInstanceOf(IssuesModel.class);
    }

    /**
     * Verifies that the SCM tables are offered only if one of the tools provides blames or forensics.
     */
    @Test
    void shouldShowScmTablesOnlyIfAvailable() {
        var checkstyle = createAction(CHECKSTYLE, createReport(createIssue(1)), new Report());
        var pmd = createAction(PMD, createReport(createIssue(2)), new Report());

        var model = new IssuesPageModel(mock(Run.class), List.of(checkstyle, pmd));

        assertThat(model).isNotBlameVisible().isNotForensicsVisible();

        Blames blames = mock(Blames.class);
        when(blames.isEmpty()).thenReturn(false);
        when(pmd.getResult().getBlames()).thenReturn(blames);

        assertThat(model).isBlameVisible().isNotForensicsVisible();

        RepositoryStatistics statistics = mock(RepositoryStatistics.class);
        when(statistics.isEmpty()).thenReturn(false);
        when(checkstyle.getResult().getForensics()).thenReturn(statistics);

        assertThat(model).isBlameVisible().isForensicsVisible();
    }

    /**
     * Verifies that the issues table contains the issues of all tools, and that each row provides the tool and the
     * baseline of the issue in addition to the properties of the row of the individual tool.
     */
    @Test
    void shouldCombineIssuesOfAllToolsInOneTable() {
        var newIssue = createIssue(1);
        var outstandingIssue = createIssue(2);
        var pmdIssue = createIssue(3);
        var checkstyle = createAction(CHECKSTYLE, createReport(newIssue, outstandingIssue), createReport(newIssue));
        var pmd = createAction(PMD, createReport(pmdIssue), new Report());

        var model = new IssuesPageModel(mock(Run.class), List.of(checkstyle, pmd));

        assertThat(model.hasIssues()).isTrue();

        var table = model.getTableModel(IssuesPageModel.ISSUES_TABLE_ID);

        assertThat(table.getId()).isEqualTo("issues");
        assertThat(table.getColumns())
                .extracting(TableColumn::getHeaderLabel)
                .containsExactly(
                        "Details",
                        "File",
                        "Package",
                        "Category",
                        "Type",
                        "Severity",
                        "Age",
                        "Hiddendetails",
                        "Hiddentool",
                        "Hiddenbaseline");
        assertThat(table.getRows()).hasSize(3);

        var json = new ObjectMapper().writeValueAsString(table.getRows());

        assertThatJson(json).isArray().hasSize(3);
        assertThatJson(json).node("[0].tool").isEqualTo(CHECKSTYLE);
        assertThatJson(json).node("[0].baseline").isEqualTo("new");
        // the HTML values are compared as plain strings, otherwise the expected values are parsed as JSON
        assertThatJson(json).node("[0].fileName.display").isString().isEqualTo(createExpectedFileName(newIssue));
        assertThatJson(json).node("[0].severity").isString().isEqualTo("<a href=\"HIGH\">High</a>");
        assertThatJson(json).node("[0].row").isAbsent();
        assertThatJson(json).node("[1].tool").isEqualTo(CHECKSTYLE);
        assertThatJson(json).node("[1].baseline").isEqualTo("outstanding");
        assertThatJson(json).node("[2].tool").isEqualTo(PMD);
        assertThatJson(json).node("[2].baseline").isEqualTo("outstanding");
        assertThatJson(json)
                .node("[2].category")
                .isString()
                .isEqualTo("<a href=\"category.1296530212/\">category-3</a>");
    }

    /**
     * Verifies that the fixed issues of all tools are shown in a table of their own. Tools that fixed all of their
     * issues are part of the page as well. The fixed issues table has no age column.
     */
    @Test
    void shouldShowFixedIssuesInTableOfTheirOwn() {
        var fixedCheckstyleIssue = createIssue(1);
        var fixedPmdIssue = createIssue(2);
        var checkstyle = createAction(CHECKSTYLE, createReport(createIssue(3)), new Report());
        setFixedIssues(checkstyle, createReport(fixedCheckstyleIssue));
        var pmd = createAction(PMD, new Report(), new Report());
        setFixedIssues(pmd, createReport(fixedPmdIssue));
        var spotbugs = createAction("spotbugs", new Report(), new Report());
        // the helper methods create mocks, so the models must be created before the stubbing starts
        var checkstyleModel = createFixedModel(checkstyle);
        var pmdModel = createFixedModel(pmd);
        TableModelFactory factory = mock(TableModelFactory.class);
        when(factory.createFixedModel(checkstyle)).thenReturn(checkstyleModel);
        when(factory.createFixedModel(pmd)).thenReturn(pmdModel);
        when(factory.createFixedColumns(anyList())).thenCallRealMethod();

        var model = new IssuesPageModel(mock(Run.class), List.of(checkstyle, pmd, spotbugs), factory);

        assertThat(model.getTools()).containsExactly(checkstyle, pmd);
        assertThat(model.hasIssues()).isTrue();
        assertThat(model.hasFixedIssues()).isTrue();
        assertThat(model.getUrlOf(pmd)).isEqualTo("table#tool=pmd");

        var table = model.getTableModel(IssuesPageModel.FIXED_TABLE_ID);

        assertThat(table.getId()).isEqualTo("fixed");
        assertThat(table.getColumns())
                .extracting(TableColumn::getHeaderLabel)
                .doesNotContain("Age")
                .startsWith("Details", "File")
                .endsWith("Severity", "Hiddendetails", "Hiddentool", "Hiddenbaseline");

        var json = new ObjectMapper().writeValueAsString(table.getRows());

        assertThatJson(json).isArray().hasSize(2);
        assertThatJson(json).node("[0].tool").isEqualTo(CHECKSTYLE);
        assertThatJson(json).node("[0].baseline").isEqualTo("fixed");
        assertThatJson(json)
                .node("[0].fileName.display")
                .isString()
                .isEqualTo(createExpectedFileName(fixedCheckstyleIssue));
        assertThatJson(json).node("[1].tool").isEqualTo(PMD);
        assertThatJson(json).node("[1].baseline").isEqualTo("fixed");
    }

    /**
     * Verifies that the links to the affected files of fixed issues are rendered only if the reference build, which
     * contains these files, is available.
     */
    @Test
    void shouldLinkFixedIssuesToFilesOfReferenceBuild() {
        var fixedIssue = createIssue(1);
        var action = createAction(CHECKSTYLE, new Report(), new Report());
        setFixedIssues(action, createReport(fixedIssue));
        var fileNameRenderer = createFileNameRenderer();
        Run<?, ?> reference = mock(Run.class);
        var labelProvider = action.getLabelProvider();
        when(labelProvider.getFileNameRenderer(reference)).thenReturn(fileNameRenderer);
        var factory = new TableModelFactory();

        var withoutReference = factory.createFixedModel(action);

        assertThat(withoutReference).isInstanceOf(FixedIssuesModel.class);
        assertThat(withoutReference.getFileNameRenderer().renderAffectedFileLink(fixedIssue))
                .isEqualTo("file-1:15");

        when(action.getResult().getReferenceBuild()).thenAnswer(invocation -> Optional.of(reference));

        var withReference = factory.createFixedModel(action);

        assertThat(withReference.getFileNameRenderer()).isSameAs(fileNameRenderer);
        assertThat(withReference.getFileNameRenderer().renderAffectedFileLink(fixedIssue))
                .isEqualTo(createExpectedFileName(fixedIssue));
    }

    /**
     * Verifies that the SCM tables provide the columns of the corresponding table models and the hidden columns
     * for tool and baseline.
     */
    @Test
    void shouldProvideColumnsOfScmTables() {
        var report = createReport(createIssue(1));
        var checkstyle = createAction(CHECKSTYLE, report, new Report());
        TableModelFactory factory = mock(TableModelFactory.class);
        var blamesModel = new BlamesModel(
                report,
                new Blames(),
                createFileNameRenderer(),
                createAgeBuilder(),
                issue -> DESCRIPTION,
                new NullDecorator(),
                createJenkinsFacade());
        when(factory.createBlamesModel(checkstyle)).thenReturn(blamesModel);
        var forensicsModel = new ForensicsModel(
                report,
                new RepositoryStatistics(),
                createFileNameRenderer(),
                createAgeBuilder(),
                issue -> DESCRIPTION,
                createJenkinsFacade());
        when(factory.createForensicsModel(checkstyle)).thenReturn(forensicsModel);

        var model = new IssuesPageModel(mock(Run.class), List.of(checkstyle), factory);

        var blames = model.getTableModel(IssuesPageModel.BLAMES_TABLE_ID);

        assertThat(blames.getId()).isEqualTo("blames");
        assertThat(blames.getColumns())
                .extracting(TableColumn::getHeaderLabel)
                .hasSize(10)
                .startsWith("Details", "File", "Age")
                .endsWith("Hiddendetails", "Hiddentool", "Hiddenbaseline");
        assertThat(blames.getRows()).hasSize(1);

        var forensics = model.getTableModel(IssuesPageModel.FORENSICS_TABLE_ID);

        assertThat(forensics.getId()).isEqualTo("forensics");
        assertThat(forensics.getColumns())
                .extracting(TableColumn::getHeaderLabel)
                .hasSize(12)
                .startsWith("Details", "File", "Age")
                .endsWith("Hiddendetails", "Hiddentool", "Hiddenbaseline");
        assertThat(forensics.getRows()).hasSize(1);
    }

    /**
     * Verifies that an unknown table ID is rejected, and that a page without tools provides only the hidden columns.
     */
    @Test
    void shouldHandleMissingTablesAndTools() {
        var model = new IssuesPageModel(mock(Run.class), List.of());

        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> model.getTableModel("unknown"));

        assertThat(model.hasIssues()).isFalse();
        assertThat(model.getTableModel(IssuesPageModel.ISSUES_TABLE_ID).getColumns())
                .extracting(TableColumn::getHeaderLabel)
                .containsExactly("Hiddentool", "Hiddenbaseline");
        assertThat(model.getTableModel(IssuesPageModel.BLAMES_TABLE_ID).getRows())
                .isEmpty();
    }

    /**
     * Verifies that the links of the age column open the results of the tool in the build that reported an issue
     * for the first time. The links are relative to the URL of the issues page, which is located two levels below
     * the current build.
     */
    @Test
    void shouldLinkAgeToResultsOfReferenceBuild() {
        var action = createAction("open-tasks", createReport(createIssue(1)), new Report());
        Job<?, ?> job = mock(Job.class);
        Run<?, ?> referenceBuild = mock(Run.class);
        when(job.getBuild("11")).thenAnswer(invocation -> referenceBuild);
        var factory = new TableModelFactory();

        var ageUrl = factory.getAgeUrl(action);
        var age = new DefaultAgeBuilder(36, ageUrl, job).apply(11);

        assertThat(age).isEqualTo("<a href=\"../../../11/open-tasks\">26</a>");
        assertThat(URI.create("http://localhost:8080/job/pipeline/36/warnings/table/")
                        .resolve("../../../11/open-tasks"))
                .hasToString("http://localhost:8080/job/pipeline/11/open-tasks");

        factory.createIssuesModel(action);
        factory.createForensicsModel(action);

        var labelProvider = action.getLabelProvider();
        verify(labelProvider).getIssuesModel(any(), eq(ageUrl), any());
        verify(labelProvider).getAgeBuilder(any(), eq(ageUrl));
    }

    /**
     * Verifies that links are resolved by the Warnings tab.
     */
    @Test
    void shouldDelegateLinksToTab() {
        var report = createReport(createIssue(1));
        var checkstyle = createAction(CHECKSTYLE, report, new Report());
        var link = "source." + report.get(0).getId();
        var sourceView = new Object();
        IssuesDetail details = mock(IssuesDetail.class);
        when(details.createDetails(link)).thenReturn(sourceView);
        when(checkstyle.getTarget()).thenReturn(details);
        Run<?, ?> run = mock(Run.class);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle));

        var model = new IssuesPageModel(run, List.of(checkstyle));

        assertThat(model.getDynamic(link)).isSameAs(sourceView);
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> model.getDynamic(CHECKSTYLE));
    }

    private Report createReport(final Issue... issues) {
        var report = new Report();
        for (Issue issue : issues) {
            report.add(issue);
        }
        return report;
    }

    private ResultAction createAction(final String id, final Report issues, final Report newIssues) {
        var action = createAction(id, issues);
        var labelProvider = mock(StaticAnalysisLabelProvider.class);
        // the helper methods create mocks, so the model must be created before the stubbing starts
        var issuesModel = new IssuesModel(
                issues, createFileNameRenderer(), createAgeBuilder(), issue -> DESCRIPTION, createJenkinsFacade());
        when(labelProvider.getIssuesModel(any(), any(), any())).thenReturn(issuesModel);
        when(action.getLabelProvider()).thenReturn(labelProvider);
        when(action.getResult().getNewIssues()).thenReturn(newIssues);
        return action;
    }

    private void setFixedIssues(final ResultAction action, final Report fixedIssues) {
        when(action.getResult().getFixedIssues()).thenReturn(fixedIssues);
        when(action.getResult().getFixedSize()).thenReturn(fixedIssues.size());
    }

    private FixedIssuesModel createFixedModel(final ResultAction action) {
        return new FixedIssuesModel(
                action.getResult().getFixedIssues(),
                createFileNameRenderer(),
                createAgeBuilder(),
                issue -> DESCRIPTION,
                createJenkinsFacade());
    }

    private ResultAction createActionWithCustomTable(
            final String id, final Report issues, final DetailsTableModel customModel) {
        var action = createAction(id, issues);
        var labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getIssuesModel(any(), any(), any())).thenReturn(customModel);
        when(action.getLabelProvider()).thenReturn(labelProvider);
        return action;
    }

    private ResultAction createAction(final String id, final Report issues) {
        ResultAction action = mock(ResultAction.class);
        AnalysisResult result = mock(AnalysisResult.class);
        Blames blames = mock(Blames.class);
        when(blames.isEmpty()).thenReturn(true);
        RepositoryStatistics statistics = mock(RepositoryStatistics.class);
        when(statistics.isEmpty()).thenReturn(true);

        when(action.getId()).thenReturn(id);
        when(action.getUrlName()).thenReturn(id);
        when(action.getDisplayName()).thenReturn(id);
        when(action.getResult()).thenReturn(result);
        when(action.getOwner()).thenAnswer(invocation -> mock(Run.class));
        when(result.getIssues()).thenReturn(issues);
        when(result.getNewIssues()).thenReturn(new Report());
        when(result.getFixedIssues()).thenReturn(new Report());
        when(result.getTotalSize()).thenReturn(issues.size());
        when(result.getBlames()).thenReturn(blames);
        when(result.getForensics()).thenReturn(statistics);
        return action;
    }
}
