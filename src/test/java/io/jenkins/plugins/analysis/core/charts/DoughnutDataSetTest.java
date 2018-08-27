package io.jenkins.plugins.analysis.core.charts;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link DoughnutDataSet}.
 *
 * @author Ullrich Hafner
 */
class DoughnutDataSetTest {
    @Test
    void shouldCreateEmptyDataSet() {
        DoughnutDataSet model = new DoughnutDataSet();

        assertThatJson(model).isEqualTo(
                "{\"data\":[],\"backgroundColor\":[],\"backgroundBorderColor\":[],\"hoverBackgroundColor\":[],\"hoverBorderColor\":[]}");
    }
    
    @Test
    void shouldMapOneElement() {
        DoughnutDataSet model = new DoughnutDataSet();
        model.add(17, Palette.RED);

        assertThatJson(model).isEqualTo("{\"data\":[17],\"backgroundColor\":[\"#EF5350\"],\"backgroundBorderColor\":[\"#fff\"],\"hoverBackgroundColor\":[\"#E53935\"],\"hoverBorderColor\":[\"#fff\"]}");
    }

    @Test
    void shouldMapTwoElements() {
        DoughnutDataSet model = new DoughnutDataSet();
        model.add(10, Palette.RED);
        model.add(20, Palette.YELLOW);

        assertThatJson(model).isEqualTo("{\"data\":[10,20],\"backgroundColor\":[\"#EF5350\",\"#FFF176\"],\"backgroundBorderColor\":[\"#fff\",\"#fff\"],\"hoverBackgroundColor\":[\"#E53935\",\"#FFEE58\"],\"hoverBorderColor\":[\"#fff\",\"#fff\"]}");
    }
}