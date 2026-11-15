package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.IssuesPageModel.TableModelFactory;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.forensics.util.CommitDecorator.NullDecorator;
import java.util.List;
import java.util.NoSuchElementException;
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
     * Verifies that the page provides the basic properties and all tools that use the default issues table.
     */
    @Test
    void shouldProvideToolsWithDefaultIssuesTable() {
        var checkstyle = createAction(CHECKSTYLE, new Report(), new Report());
        var pmd = createAction(PMD, new Report(), new Report());
        var cpd = createActionWithCustomTable("cpd");
        Run<?, ?> run = mock(Run.class);

        var model = new IssuesPageModel(run, List.of(checkstyle, cpd, pmd));

        assertThat(model).hasDisplayName("Issues").hasUrlName("table").hasAllToolsId(TrendModel.ALL_TOOLS_ID);
        assertThat(model.getObject()).isSameAs(run);
        assertThat(model.getTab().getObject()).isSameAs(run);
        assertThat(model.getTools()).containsExactly(checkstyle, pmd);
        assertThat(model.hasSeveralTools()).isTrue();
        assertThat(model.hasIssues()).isFalse();

        var single = new IssuesPageModel(run, List.of(checkstyle, cpd));

        assertThat(single.getTools()).containsExactly(checkstyle);
        assertThat(single.hasSeveralTools()).isFalse();
    }

    /**
     * Verifies that the SCM tables are offered only if one of the tools provides blames or forensics.
     */
    @Test
    void shouldShowScmTablesOnlyIfAvailable() {
        var checkstyle = createAction(CHECKSTYLE, new Report(), new Report());
        var pmd = createAction(PMD, new Report(), new Report());

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
     * Verifies that the SCM tables provide the columns of the corresponding table models and the hidden columns
     * for tool and baseline.
     */
    @Test
    void shouldProvideColumnsOfScmTables() {
        var report = new Report();
        var checkstyle = createAction(CHECKSTYLE, report, new Report());
        TableModelFactory factory = mock(TableModelFactory.class);
        when(factory.hasDefaultIssuesTable(checkstyle)).thenReturn(true);
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
        assertThat(blames.getRows()).isEmpty();

        var forensics = model.getTableModel(IssuesPageModel.FORENSICS_TABLE_ID);

        assertThat(forensics.getId()).isEqualTo("forensics");
        assertThat(forensics.getColumns())
                .extracting(TableColumn::getHeaderLabel)
                .hasSize(12)
                .startsWith("Details", "File", "Age")
                .endsWith("Hiddendetails", "Hiddentool", "Hiddenbaseline");
        assertThat(forensics.getRows()).isEmpty();
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
     * Verifies that links are resolved by the Warnings tab.
     */
    @Test
    void shouldDelegateLinksToTab() {
        var checkstyle = createAction(CHECKSTYLE, new Report(), new Report());
        Run<?, ?> run = mock(Run.class);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(checkstyle));

        var model = new IssuesPageModel(run, List.of(checkstyle));

        assertThat(model.getDynamic(CHECKSTYLE)).isSameAs(checkstyle);
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> model.getDynamic("missing"));
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

    private ResultAction createActionWithCustomTable(final String id) {
        var action = createAction(id, new Report());
        var labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getIssuesModel(any(), any(), any())).thenReturn(mock(DetailsTableModel.class));
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
        when(result.getBlames()).thenReturn(blames);
        when(result.getForensics()).thenReturn(statistics);
        return action;
    }
}
