package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;

/**
 * Tests the class {@link LineModel}.
 *
 * @author Ullrich Hafner
 */
class LineModelTest {
    private static final String COLOR = "#fff";

    @Test
    void shouldCreateLineModel() {
        LineModel model = new LineModel("spotbugs");
        List<String> builds = new ArrayList<>();
        List<LineSeries> series = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            builds.add("#" + (i + 1));
        }
        series.add(new LineSeries("High", COLOR));
        series.add(new LineSeries("Normal", COLOR));
        series.add(new LineSeries("Low", COLOR));

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