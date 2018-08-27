package io.jenkins.plugins.analysis.core.charts;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import net.sf.json.JSONObject;

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

        assertThatJson(model).node("labels").isArray().ofLength(1).thatContains("High");
        assertThatJson(model).node("urls").isArray().ofLength(1).thatContains("highUrl");

        DoughnutDataSet dataSet = new DoughnutDataSet();
        dataSet.add(17, Palette.RED);
        assertThatJson(model).node("datasets").isArray().ofLength(1).thatContains(JSONObject.fromObject(dataSet));
    }

    @Test
    void shouldCreateTwoPriorities() {
        DoughnutModel model = new DoughnutModel();
        model.add("High", 10, "highUrl", Palette.RED);
        model.add("Normal", 20, "normalUrl", Palette.BLUE);

        assertThatJson(model).node("labels").isArray().ofLength(2).thatContains("High").thatContains("Normal");
        assertThatJson(model).node("urls").isArray().ofLength(2).thatContains("highUrl").thatContains("normalUrl");

        DoughnutDataSet dataSet = new DoughnutDataSet();
        dataSet.add(10, Palette.RED);
        dataSet.add(20, Palette.BLUE);
        assertThatJson(model).node("datasets").isArray().ofLength(1).thatContains(JSONObject.fromObject(dataSet));
    }
}