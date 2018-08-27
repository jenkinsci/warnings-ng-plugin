package io.jenkins.plugins.analysis.core.charts;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link DoughnutDataSet}.
 *
 * @author Ullrich Hafner
 */
class DoughnutDataSetTest {
    private static final String BORDER_COLOR = "#fff";

    @Test
    void shouldCreateEmptyDataSet() {
        DoughnutDataSet model = new DoughnutDataSet();

        assertThatJson(model).node("data").isArray().ofLength(0);
        assertThatJson(model).node("backgroundColor").isArray().ofLength(0);
        assertThatJson(model).node("hoverBackgroundColor").isArray().ofLength(0);
        assertThatJson(model).node("backgroundBorderColor").isArray().ofLength(0);
        assertThatJson(model).node("hoverBorderColor").isArray().ofLength(0);
    }
    
    @Test
    void shouldMapOneElement() {
        DoughnutDataSet model = new DoughnutDataSet();
        model.add(17, Palette.RED);

        assertThatJson(model).node("data").isArray().ofLength(1).thatContains(17);
        assertThatJson(model).node("backgroundColor").isArray().ofLength(1).thatContains(Palette.RED.getNormal());
        assertThatJson(model).node("hoverBackgroundColor").isArray().ofLength(1).thatContains(Palette.RED.getHover());
        assertThatJson(model).node("backgroundBorderColor").isArray().ofLength(1).thatContains(BORDER_COLOR);
        assertThatJson(model).node("hoverBorderColor").isArray().ofLength(1).thatContains(BORDER_COLOR);
    }

    @Test
    void shouldMapTwoElements() {
        DoughnutDataSet model = new DoughnutDataSet();
        model.add(10, Palette.RED);
        model.add(20, Palette.BLUE);

        assertThatJson(model).node("data").isArray().ofLength(2).thatContains(10).thatContains(20);
        assertThatJson(model).node("backgroundColor").isArray().ofLength(2).thatContains(Palette.RED.getNormal()).thatContains(Palette.BLUE.getNormal());
        assertThatJson(model).node("hoverBackgroundColor").isArray().ofLength(2).thatContains(Palette.RED.getHover()).thatContains(Palette.BLUE.getHover());
        assertThatJson(model).node("backgroundBorderColor").isArray().ofLength(2).thatContains(BORDER_COLOR);
        assertThatJson(model).node("hoverBorderColor").isArray().ofLength(2).thatContains(BORDER_COLOR);
    }
}