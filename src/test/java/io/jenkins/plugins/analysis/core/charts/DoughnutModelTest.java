package io.jenkins.plugins.analysis.core.charts;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link DoughnutModel}.
 *
 * @author Ullrich Hafner
 */
class DoughnutModelTest {
    @Test
    void shouldCreateOnePriority() {
        DoughnutModel model = new DoughnutModel();
        model.add("High", 17, "highUrl", Palette.RED);

        assertThatJson(model).isEqualTo("{\"labels\":[\"High\"],\"urls\":[\"highUrl\"],\"datasets\":[{\"data\":[17],\"backgroundColor\":[\"#EF5350\"],\"backgroundBorderColor\":[\"#e0e0e0\"],\"hoverBackgroundColor\":[\"#E53935\"],\"hoverBorderColor\":[\"#e0e0e0\"]}]}");
    }

    @Test
    void shouldCreateTwoPriorities() {
        DoughnutModel model = new DoughnutModel();
        model.add("High", 10, "highUrl", Palette.RED);
        model.add("Normal", 20, "normalUrl", Palette.YELLOW);

        assertThatJson(model).isEqualTo("{\"labels\":[\"High\",\"Normal\"],\"urls\":[\"highUrl\",\"normalUrl\"],\"datasets\":[{\"data\":[10,20],\"backgroundColor\":[\"#EF5350\",\"#FFF176\"],\"backgroundBorderColor\":[\"#e0e0e0\",\"#e0e0e0\"],\"hoverBackgroundColor\":[\"#E53935\",\"#FFEE58\"],\"hoverBorderColor\":[\"#e0e0e0\",\"#e0e0e0\"]}]}");
    }
}