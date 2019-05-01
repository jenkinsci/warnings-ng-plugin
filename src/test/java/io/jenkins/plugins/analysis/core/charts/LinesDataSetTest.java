package io.jenkins.plugins.analysis.core.charts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testclass for LinesDataSet.
 *
 * @author Lorenz Munsch
 */
class LinesDataSetTest {
    private static final String X_AXIS_LABEL = "X_AXIS_LABEL";
    private static final String FIRST_DATA_SET = "Eins";
    private static final String SECOND_DATA_SET = "Zwei";

    @Test
    void shouldAddAndVerifySeries() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        assertThat(linesDataSet.getXAxisSize()).isEqualTo(0);
        assertThat(linesDataSet.hasSeries(FIRST_DATA_SET)).isFalse();

        linesDataSet.add(X_AXIS_LABEL, createSeries());
        assertThat(linesDataSet.getXAxisSize()).isEqualTo(1);
        assertThat(linesDataSet.getXAxisLabels()).containsOnly(X_AXIS_LABEL);
        assertThat(linesDataSet.getDataSetIds()).containsOnlyOnce(FIRST_DATA_SET, SECOND_DATA_SET);

        assertThat(linesDataSet.hasSeries(FIRST_DATA_SET)).isTrue();
        assertThat(linesDataSet.getSeries(FIRST_DATA_SET)).containsOnly(1);

        assertThat(linesDataSet.hasSeries(SECOND_DATA_SET)).isTrue();
        assertThat(linesDataSet.getSeries(SECOND_DATA_SET)).containsOnly(2);
    }

    @Test
    void shouldThrowExceptionIfSeriesDoesNotExist() {
        LinesDataSet linesDataSet  = new LinesDataSet();

        linesDataSet.add(X_AXIS_LABEL, createSeries());

        assertThatThrownBy(() -> linesDataSet.getSeries("WrongId"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("No dataset")
                .hasMessageContaining("registered");
    }

    @Test
    void shouldThrowExceptionIfSameLabelIsAddedTwice() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        Map<String, Integer>  dataSetSeries = Collections.emptyMap();

        linesDataSet.add(X_AXIS_LABEL, dataSetSeries);
        assertThatThrownBy(() -> linesDataSet.add(X_AXIS_LABEL, dataSetSeries))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Label already registered:");
    }

    private Map<String, Integer> createSeries() {
        Map<String, Integer> dataSetSeries = new HashMap<>();
        dataSetSeries.put(FIRST_DATA_SET, 1);
        dataSetSeries.put(SECOND_DATA_SET, 2);

        return dataSetSeries;
    }

}