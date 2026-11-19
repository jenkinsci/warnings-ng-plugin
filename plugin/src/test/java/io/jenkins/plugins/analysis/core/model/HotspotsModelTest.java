package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.HotspotsModel.Baseline;
import io.jenkins.plugins.analysis.core.model.HotspotsModel.Grouping;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.ObjectMapper;

/**
 * Tests the class {@link HotspotsModel}.
 *
 * @author Ullrich Hafner
 */
class HotspotsModelTest {
    private static final String CHECKSTYLE = "checkstyle";
    private static final String PMD = "pmd";

    /**
     * Verifies that the issues are grouped by file by default and that the files with the most issues come first.
     */
    @Test
    void shouldGroupIssuesByFileAndSortByTotal() {
        var model = createModel(createAction(CHECKSTYLE, createReport()));

        var chart = model.createHotspots("{}");

        assertThat(chart).hasNumberOfElements(3);
        assertThat(chart.getSeverities())
                .containsExactly(
                        entry("error", "Error"), entry("high", "High"), entry("normal", "Normal"), entry("low", "Low"));
        assertThat(chart.getColors()).containsExactly(
                entry("error", "--red"), entry("high", "--pink"), entry("normal", "--orange"), entry("low", "--yellow"));

        var hotspots = chart.getHotspots();
        assertThat(hotspots).hasSize(3);
        assertThat(hotspots.get(0)).hasName("/src/A.java").hasLabel("A.java").hasTotal(3);
        assertThat(hotspots.get(0).getCounts())
                .containsExactly(entry("error", 1), entry("high", 0), entry("normal", 2), entry("low", 0));
        assertThat(hotspots.get(1)).hasName("/src/C.java").hasLabel("C.java").hasTotal(2);
        assertThat(hotspots.get(2)).hasName("/src/B.java").hasLabel("B.java").hasTotal(1);

        var buckets = chart.getBuckets();
        assertThat(buckets)
                .extracting(HotspotsModel.Bucket::getLabel)
                .containsExactly("1", "2–5", "6–10", "11–20", "21–50", "> 50");
        assertThat(buckets).extracting(HotspotsModel.Bucket::getCount).containsExactly(1, 2, 0, 0, 0, 0);
    }

    /**
     * Verifies that only the requested number of hotspots is returned, while the histogram still considers all
     * elements.
     */
    @Test
    void shouldLimitNumberOfHotspots() {
        var model = createModel(createAction(CHECKSTYLE, createReport()));

        var chart = model.createHotspots("{\"numberOfHotspots\": 2}");

        assertThat(chart).hasNumberOfElements(3);
        assertThat(chart.getHotspots())
                .extracting(HotspotsModel.Hotspot::getLabel)
                .containsExactly("A.java", "C.java");
        assertThat(chart.getBuckets())
                .extracting(HotspotsModel.Bucket::getCount)
                .containsExactly(1, 2, 0, 0, 0, 0);

        assertThat(model.createHotspots("{\"numberOfHotspots\": 0}").getHotspots())
                .hasSize(1);
    }

    /**
     * Verifies that the issues of the selected tool and baseline are grouped by the selected property.
     */
    @Test
    void shouldSelectToolBaselineAndGrouping() {
        var checkstyle = createAction(CHECKSTYLE, createReport());
        var pmd = createAction(PMD, createReport());
        try (var builder = new IssueBuilder()) {
            var newIssues = new Report();
            newIssues.add(builder.setFileName("/src/D.java")
                    .setCategory("Design")
                    .setLineStart(1)
                    .build());
            newIssues.add(builder.setFileName("/src/D.java")
                    .setCategory("Design")
                    .setLineStart(2)
                    .build());
            newIssues.add(builder.setFileName("/src/E.java")
                    .setCategory("Naming")
                    .setLineStart(3)
                    .build());
            when(pmd.getResult().getNewIssues()).thenReturn(newIssues);
        }

        var model = createModel(checkstyle, pmd);

        var chart = model.createHotspots("{\"tool\": \"pmd\", \"baseline\": \"new\", \"grouping\": \"category\"}");

        assertThat(chart).hasNumberOfElements(2);
        assertThat(chart.getHotspots())
                .extracting(HotspotsModel.Hotspot::getName)
                .containsExactly("Design", "Naming");
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
        var checkstyle = createAction(CHECKSTYLE, createReport());
        try (var builder = new IssueBuilder()) {
            var report = new Report();
            report.add(builder.setFileName("/src/B.java").setLineStart(42).build());
            var pmd = createAction(PMD, report);

            var chart = createModel(checkstyle, pmd).createHotspots("{\"tool\": \"" + tool + "\"}");

            assertThat(chart.getHotspots())
                    .extracting(HotspotsModel.Hotspot::getLabel)
                    .containsExactly("A.java", "B.java", "C.java");
        }
    }

    /**
     * Verifies that invalid configurations are replaced by the defaults.
     *
     * @param configuration
     *         the invalid configuration
     */
    @ParameterizedTest(name = "configuration = {0}")
    @ValueSource(strings = {"", "[]", "nope", "{\"grouping\": \"unknown\", \"baseline\": 1}"})
    void shouldUseDefaultsForInvalidConfiguration(final String configuration) {
        var model = createModel(createAction(CHECKSTYLE, createReport()));

        assertThat(model.createHotspots(configuration)).hasNumberOfElements(3);
    }

    /**
     * Verifies that the hotspots are serialized as JSON for the client side.
     */
    @Test
    void shouldSerializeHotspotsAsJson() {
        var model = createModel(createAction(CHECKSTYLE, createReport()));

        var json = new ObjectMapper().readTree(model.getHotspots("{}"));

        assertThat(json.get("numberOfElements").asInt()).isEqualTo(3);
        assertThat(json.get("hotspots").get(0).get("label").asString()).isEqualTo("A.java");
        assertThat(json.get("hotspots").get(0).get("counts").get("normal").asInt())
                .isEqualTo(2);
        assertThat(json.get("buckets").get(1).get("count").asInt()).isEqualTo(2);
        assertThat(json.get("severities").get("error").asString()).isEqualTo("Error");
        assertThat(json.get("colors").get("error").asString()).isEqualTo("--red");
    }

    /**
     * Verifies that tools without issues are not listed.
     */
    @Test
    void shouldListOnlyToolsWithIssues() {
        var checkstyle = createAction(CHECKSTYLE, createReport());
        var pmd = createAction(PMD, new Report());

        var model = createModel(checkstyle, pmd);

        assertThat(model)
                .hasDisplayName("Hotspots")
                .hasUrlName("hotspots")
                .hasAllToolsId("*")
                .hasIssues()
                .doesNotHaveSeveralTools()
                .hasTools(checkstyle);
        assertThat(model.getGroupings()).containsExactly(Grouping.values());
        assertThat(model.getBaselines()).containsExactly(Baseline.values());
        assertThat(Grouping.FILE.getDisplayName()).isEqualTo("Files");
        assertThat(Baseline.OUTSTANDING.getDisplayName()).isEqualTo("Outstanding issues");
    }

    /**
     * Verifies that a run without issues has no hotspots.
     */
    @Test
    void shouldHandleRunWithoutIssues() {
        var model = createModel(createAction(PMD, new Report()));

        assertThat(model).doesNotHaveIssues().hasNoTools();

        var chart = model.createHotspots("{}");
        assertThat(chart).hasNumberOfElements(0).hasNoHotspots();
        assertThat(chart.getBuckets())
                .extracting(HotspotsModel.Bucket::getCount)
                .containsOnly(0);
    }

    /**
     * Creates a model for a run with the specified results.
     *
     * @param actions
     *         the results of the run
     *
     * @return the model
     */
    private HotspotsModel createModel(final ResultAction... actions) {
        return new HotspotsModel(mock(Run.class), List.of(actions));
    }

    /**
     * Creates a report with three files: A.java (one error and two normal issues), B.java (one high issue), and
     * C.java (two low issues).
     *
     * @return the report
     */
    private Report createReport() {
        try (var builder = new IssueBuilder()) {
            var report = new Report();
            report.add(builder.setFileName("/src/A.java")
                    .setSeverity(Severity.ERROR)
                    .setLineStart(1)
                    .build());
            report.add(builder.setFileName("/src/A.java")
                    .setSeverity(Severity.WARNING_NORMAL)
                    .setLineStart(2)
                    .build());
            report.add(builder.setFileName("/src/A.java")
                    .setSeverity(Severity.WARNING_NORMAL)
                    .setLineStart(3)
                    .build());
            report.add(builder.setFileName("/src/B.java")
                    .setSeverity(Severity.WARNING_HIGH)
                    .setLineStart(4)
                    .build());
            report.add(builder.setFileName("/src/C.java")
                    .setSeverity(Severity.WARNING_LOW)
                    .setLineStart(5)
                    .build());
            report.add(builder.setFileName("/src/C.java")
                    .setSeverity(Severity.WARNING_LOW)
                    .setLineStart(6)
                    .build());
            return report;
        }
    }

    /**
     * Creates a result action stub with the specified issues. The issues are used for all baselines.
     *
     * @param id
     *         the ID of the action
     * @param issues
     *         the issues of the action
     *
     * @return the action
     */
    private ResultAction createAction(final String id, final Report issues) {
        var action = mock(ResultAction.class);
        var result = mock(AnalysisResult.class);

        when(action.getId()).thenReturn(id);
        when(action.getResult()).thenReturn(result);
        when(result.getTotalSize()).thenReturn(issues.size());
        when(result.getIssues()).thenReturn(issues);
        when(result.getNewIssues()).thenReturn(issues);
        when(result.getOutstandingIssues()).thenReturn(issues);

        return action;
    }
}
