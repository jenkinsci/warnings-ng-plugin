package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.HotspotsModel.Baseline;
import io.jenkins.plugins.analysis.core.model.TreeMapModel.Node;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.ObjectMapper;

/**
 * Tests the class {@link TreeMapModel}.
 *
 * @author Ullrich Hafner
 */
class TreeMapModelTest {
    private static final String CHECKSTYLE = "checkstyle";
    private static final String PMD = "pmd";
    private static final String PACKAGE_A = "edu.hm.a";
    private static final String PACKAGE_B = "edu.hm.b";

    /**
     * Verifies that the issues are grouped by package and file, and that the number of issues and new issues is
     * summed up for each level.
     */
    @Test
    void shouldBuildHierarchyOfPackagesAndFiles() {
        var report = createReport();
        var newIssues = new Report();
        newIssues.add(report.get(2));
        var model = createModel(createAction(CHECKSTYLE, report, newIssues));

        var chart = model.createTreeMap("{}");

        assertThat(chart.getSeverities())
                .containsExactly(
                        entry("error", "Error"), entry("high", "High"), entry("normal", "Normal"), entry("low", "Low"));
        assertThat(chart.getColors())
                .containsExactly(
                        entry("error", "--red"),
                        entry("high", "--pink"),
                        entry("normal", "--orange"),
                        entry("low", "--yellow"));

        var root = chart.getRoot();
        assertThat(root).hasName("").hasTotal(4).hasNumberOfNewIssues(1);
        assertThat(root.getChildren()).extracting(Node::getName).containsExactly(PACKAGE_A, PACKAGE_B);

        var packageA = root.getChildren().get(0);
        assertThat(packageA).hasLabel(PACKAGE_A).hasTotal(3).hasNumberOfNewIssues(1);
        assertThat(packageA.getChildren()).extracting(Node::getLabel).containsExactly("A.java", "B.java");

        var fileA = packageA.getChildren().get(0);
        assertThat(fileA).hasName("/src/a/A.java").hasTotal(2).hasNumberOfNewIssues(0);
        assertThat(fileA.getCounts())
                .containsExactly(entry("error", 1), entry("high", 0), entry("normal", 1), entry("low", 0));
        assertThat(fileA.getChildren()).isEmpty();

        var fileB = packageA.getChildren().get(1);
        assertThat(fileB).hasName("/src/a/B.java").hasTotal(1).hasNumberOfNewIssues(1);
        assertThat(fileB.getCounts()).containsEntry("high", 1);

        var packageB = root.getChildren().get(1);
        assertThat(packageB).hasTotal(1).hasNumberOfNewIssues(0);
        assertThat(packageB.getChildren()).extracting(Node::getLabel).containsExactly("C.java");
    }

    /**
     * Verifies that the folder of the affected file is used if an issue has no package.
     */
    @Test
    void shouldUseFolderIfIssueHasNoPackage() {
        try (var builder = new IssueBuilder()) {
            var report = new Report();
            Issue issue =
                    builder.setFileName("src/main/js/app.js").setLineStart(1).build();
            report.add(issue);

            var root = createModel(createAction(CHECKSTYLE, report, new Report()))
                    .createTreeMap("{}")
                    .getRoot();

            assertThat(root.getChildren()).hasSize(1);
            var folder = root.getChildren().get(0);
            assertThat(folder)
                    .hasName(issue.getFolder())
                    .hasLabel(issue.getFolder())
                    .hasTotal(1);
            assertThat(folder.getChildren()).extracting(Node::getLabel).containsExactly("app.js");
        }
    }

    /**
     * Verifies that the modules are the first level of the hierarchy if the issues are spread over several modules.
     */
    @Test
    void shouldAddModuleLevelForSeveralModules() {
        try (var builder = new IssueBuilder()) {
            var report = new Report();
            report.add(builder.setModuleName("core")
                    .setPackageName(PACKAGE_A)
                    .setFileName("/core/A.java")
                    .setLineStart(1)
                    .build());
            report.add(builder.setModuleName("api")
                    .setPackageName(PACKAGE_B)
                    .setFileName("/api/B.java")
                    .setLineStart(2)
                    .build());

            var root = createModel(createAction(CHECKSTYLE, report, new Report()))
                    .createTreeMap("{}")
                    .getRoot();

            assertThat(root.getChildren()).extracting(Node::getName).containsExactly("api", "core");
            var api = root.getChildren().get(0);
            assertThat(api).hasTotal(1);
            assertThat(api.getChildren()).extracting(Node::getName).containsExactly(PACKAGE_B);
            assertThat(api.getChildren().get(0).getChildren())
                    .extracting(Node::getLabel)
                    .containsExactly("B.java");
        }
    }

    /**
     * Verifies that only the issues of the selected tool and baseline are shown.
     */
    @Test
    void shouldSelectToolAndBaseline() {
        var checkstyle = createAction(CHECKSTYLE, createReport(), new Report());
        var pmdReport = createReport();
        var pmdNew = new Report();
        pmdNew.add(pmdReport.get(3));
        var pmd = createAction(PMD, pmdReport, pmdNew);

        var model = createModel(checkstyle, pmd);

        var root = model.createTreeMap("{\"tool\": \"pmd\", \"baseline\": \"new\"}")
                .getRoot();
        assertThat(root).hasTotal(1).hasNumberOfNewIssues(1);
        assertThat(root.getChildren()).extracting(Node::getName).containsExactly(PACKAGE_B);
    }

    /**
     * Verifies that the issues of all tools are combined if all tools (or an unknown tool) are selected.
     *
     * @param tool
     *         the selected tool
     */
    @ParameterizedTest(name = "tool = {0}")
    @ValueSource(strings = {"*", "unknown"})
    void shouldCombineAllTools(final String tool) {
        var checkstyle = createAction(CHECKSTYLE, createReport(), new Report());
        try (var builder = new IssueBuilder()) {
            var report = new Report();
            report.add(builder.setPackageName("edu.hm.c")
                    .setFileName("/src/c/D.java")
                    .setLineStart(42)
                    .build());
            var pmd = createAction(PMD, report, report);

            var root = createModel(checkstyle, pmd)
                    .createTreeMap("{\"tool\": \"" + tool + "\"}")
                    .getRoot();

            assertThat(root).hasTotal(5).hasNumberOfNewIssues(1);
            assertThat(root.getChildren()).extracting(Node::getName).containsExactly(PACKAGE_A, PACKAGE_B, "edu.hm.c");
        }
    }

    /**
     * Verifies that invalid configurations are replaced by the defaults.
     *
     * @param configuration
     *         the invalid configuration
     */
    @ParameterizedTest(name = "configuration = {0}")
    @ValueSource(strings = {"", "[]", "nope", "{\"tool\": 1, \"baseline\": \"unknown\"}"})
    void shouldUseDefaultsForInvalidConfiguration(final String configuration) {
        var model = createModel(createAction(CHECKSTYLE, createReport(), new Report()));

        assertThat(model.createTreeMap(configuration).getRoot()).hasTotal(4);
    }

    /**
     * Verifies that the treemap is serialized as JSON for the client side.
     */
    @Test
    void shouldSerializeTreeMapAsJson() {
        var report = createReport();
        var model = createModel(createAction(CHECKSTYLE, report, report));

        var json = new ObjectMapper().readTree(model.getTreeMap("{}"));

        var root = json.get("root");
        assertThat(root.get("total").asInt()).isEqualTo(4);
        assertThat(root.get("numberOfNewIssues").asInt()).isEqualTo(4);
        var file = root.get("children").get(0).get("children").get(0);
        assertThat(file.get("name").asString()).isEqualTo("/src/a/A.java");
        assertThat(file.get("label").asString()).isEqualTo("A.java");
        assertThat(file.get("counts").get("error").asInt()).isEqualTo(1);
        assertThat(file.get("children")).isEmpty();
        assertThat(json.get("severities").get("low").asString()).isEqualTo("Low");
        assertThat(json.get("colors").get("high").asString()).isEqualTo("--pink");
    }

    /**
     * Verifies that tools without issues are not listed.
     */
    @Test
    void shouldListOnlyToolsWithIssues() {
        var checkstyle = createAction(CHECKSTYLE, createReport(), new Report());
        var pmd = createAction(PMD, new Report(), new Report());

        var model = createModel(checkstyle, pmd);

        assertThat(model)
                .hasDisplayName("Treemap")
                .hasUrlName("treemap")
                .hasAllToolsId("*")
                .hasIssues()
                .doesNotHaveSeveralTools()
                .hasTools(checkstyle);
        assertThat(model.getBaselines()).containsExactly(Baseline.values());
    }

    /**
     * Verifies that a run without issues has an empty treemap.
     */
    @Test
    void shouldHandleRunWithoutIssues() {
        var model = createModel(createAction(PMD, new Report(), new Report()));

        assertThat(model).doesNotHaveIssues().hasNoTools();

        var root = model.createTreeMap("{}").getRoot();
        assertThat(root).hasTotal(0).hasNumberOfNewIssues(0);
        assertThat(root.getChildren()).isEmpty();
    }

    /**
     * Creates a model for a run with the specified results.
     *
     * @param actions
     *         the results of the run
     *
     * @return the model
     */
    private TreeMapModel createModel(final ResultAction... actions) {
        return new TreeMapModel(mock(Run.class), List.of(actions));
    }

    /**
     * Creates a report with two packages: {@value #PACKAGE_A} with the files A.java (one error and one normal issue)
     * and B.java (one high issue), and {@value #PACKAGE_B} with the file C.java (one low issue).
     *
     * @return the report
     */
    private Report createReport() {
        try (var builder = new IssueBuilder()) {
            var report = new Report();
            report.add(builder.setPackageName(PACKAGE_A)
                    .setFileName("/src/a/A.java")
                    .setSeverity(Severity.ERROR)
                    .setLineStart(1)
                    .build());
            report.add(builder.setPackageName(PACKAGE_A)
                    .setFileName("/src/a/A.java")
                    .setSeverity(Severity.WARNING_NORMAL)
                    .setLineStart(2)
                    .build());
            report.add(builder.setPackageName(PACKAGE_A)
                    .setFileName("/src/a/B.java")
                    .setSeverity(Severity.WARNING_HIGH)
                    .setLineStart(3)
                    .build());
            report.add(builder.setPackageName(PACKAGE_B)
                    .setFileName("/src/b/C.java")
                    .setSeverity(Severity.WARNING_LOW)
                    .setLineStart(4)
                    .build());
            return report;
        }
    }

    /**
     * Creates a result action stub with the specified issues. All issues are used for the baselines "all" and
     * "outstanding", the specified new issues for the baseline "new".
     *
     * @param id
     *         the ID of the action
     * @param issues
     *         the issues of the action
     * @param newIssues
     *         the new issues of the action
     *
     * @return the action
     */
    private ResultAction createAction(final String id, final Report issues, final Report newIssues) {
        var action = mock(ResultAction.class);
        var result = mock(AnalysisResult.class);

        when(action.getId()).thenReturn(id);
        when(action.getResult()).thenReturn(result);
        when(result.getTotalSize()).thenReturn(issues.size());
        when(result.getIssues()).thenReturn(issues);
        when(result.getNewIssues()).thenReturn(newIssues);
        when(result.getOutstandingIssues()).thenReturn(issues);

        return action;
    }
}
