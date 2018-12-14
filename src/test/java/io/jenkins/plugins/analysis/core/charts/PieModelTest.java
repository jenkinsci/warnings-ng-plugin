package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.sf.json.JSONArray;

/**
 * Tests the class {@link PieModel}.
 *
 * @author Ullrich Hafner
 */
class PieModelTest {
    @Test
    void shouldConvertListOfPointsToJson() {
        List<PieModel> models = new ArrayList<>();
        PieModel first = new PieModel("ONE", 1);
        PieModel second = new PieModel("TWO", 2);
        models.add(first);
        models.add(second);

        JSONArray array = JSONArray.fromObject(models);

        assertThatJson(array).isArray().hasSize(2)
                .contains(first)
                .contains(second);
    }
}