package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.charts.LineSeries.FilledMode;
import io.jenkins.plugins.analysis.core.charts.LineSeries.StackedMode;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link LinesChartModel}.
 *
 * @author Ullrich Hafner
 */
class LinesChartModelTest {
    private static final String COLOR = "#fff";

    @Test
    void testEmptyCtor() {
        LinesChartModel model = new LinesChartModel();
        assertThat(model.getId()).isEqualTo("");
        assertThat(model.getSeries().size()).isEqualTo(0);
        assertThat(model.getXAxisLabels()).isEqualTo(Collections.EMPTY_LIST);

    }

    @Test
    void testToString() {
        LinesChartModel model = new LinesChartModel("spotbugs");
        model.addXAxisLabels(Arrays.asList("1", "2", "3"));
        assertThat(model.toString()).isEqualTo("{\n"
                + "  \"XAxisLabels\":   [\n"
                + "    \"1\",\n"
                + "    \"2\",\n"
                + "    \"3\"\n"
                + "  ],\n"
                + "  \"id\": \"spotbugs\",\n"
                + "  \"series\": []\n"
                + "}");
    }

    @Test
    void testSize() {
        LinesChartModel model = new LinesChartModel("spotbugs");
        model.addXAxisLabels(Arrays.asList("1", "2", "3"));
        assertThat(model.size()).isEqualTo(3);

    }

    @Test
    void testGetId() {
        LinesChartModel model = new LinesChartModel("spotbugs");
        assertThat(model.getId()).isEqualTo(("spotbugs"));

        model.setId("anotherSpotbugs");
        assertThat(model.getId()).isEqualTo(("anotherSpotbugs"));
    }

    @Test
    void testGetSeries() {
        LinesChartModel model = new LinesChartModel("spotbugs");
        LineSeries series = new LineSeries("TestName", "TestColor", StackedMode.STACKED, FilledMode.FILLED);
        model.addSeries(series);

        assertThat(model.getSeries().get(0)).isEqualTo(series);
    }

    @Test
    void testGetXAxisLabels() {
        LinesChartModel modelForSingleXAxisLabelTest = new LinesChartModel("spotbugs");
        modelForSingleXAxisLabelTest.addXAxisLabel("a");
        assertThat(modelForSingleXAxisLabelTest.getXAxisLabels()).isEqualTo(Arrays.asList("a"));

        LinesChartModel modelForXAxisListLabelTest = new LinesChartModel("spotbugs");
        modelForXAxisListLabelTest.addXAxisLabels(Arrays.asList("a", "b", "c"));
        assertThat(modelForXAxisListLabelTest.getXAxisLabels()).isEqualTo(Arrays.asList("a", "b", "c"));
    }

    @Test
    void shouldCreateLineModel() {
        LinesChartModel model = new LinesChartModel("spotbugs");
        List<String> builds = new ArrayList<>();
        List<LineSeries> series = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            builds.add("#" + (i + 1));
        }
        series.add(new LineSeries("High", COLOR, StackedMode.STACKED, FilledMode.FILLED));
        series.add(new LineSeries("Normal", COLOR, StackedMode.STACKED, FilledMode.FILLED));
        series.add(new LineSeries("Low", COLOR, StackedMode.STACKED, FilledMode.FILLED));

        for (LineSeries severity : series) {
            for (int i = 0; i < 5; i++) {
                severity.add(i * 10);
            }
        }

        model.addXAxisLabels(builds);
        model.addSeries(series);



        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(5)
                .contains("#1")
                .contains("#2")
                .contains("#3")
                .contains("#4")
                .contains("#5");

        assertThatJson(model).node("series").isArray().hasSize(3);
    }
}