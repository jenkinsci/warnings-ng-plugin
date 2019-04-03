package io.jenkins.plugins.analysis.core.charts;

import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;

/**
 * Tests the class {@link PieChartModel}.
 *
 * @author Nils Engelbrecht
 */
class PieChartModelTest {
    @Test
    void shouldCreatePieModel() {
        PieChartModel model = new PieChartModel("pieChartName");
        PieData first = new PieData("ONE", 1);
        PieData second = new PieData("TWO", 2);

        model.add(first, Palette.BLUE);
        model.add(second, Palette.RED);

        assertThatJson(model).node("data")
                .isArray().hasSize(2)
                .contains(first)
                .contains(second);

        assertThatJson(model).node("colors")
                .isArray().hasSize(2)
                .contains(Palette.BLUE.getNormal())
                .contains(Palette.RED.getNormal());

        assertThatJson(model).node("name").isEqualTo("pieChartName");
    }
}