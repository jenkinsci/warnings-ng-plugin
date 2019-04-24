package io.jenkins.plugins.analysis.core.charts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Testclass for LinesDataSet.
 *
 * @author Lorenz Munsch
 */
class LinesDataSetTest {

    private static final String TESTLABEL = "Testlabel";

    private Map<String, Integer> getdataSetSeriesMap() {
        Map<String, Integer>  dataSetSeries = new HashMap<>();
        dataSetSeries.put("Eins", 1);
        dataSetSeries.put("Zwei", 2);
        dataSetSeries.put("Drei", 3);
        dataSetSeries.put("Vier", 4);

        return dataSetSeries;
    }

    @Test
    void shouldGetXAxisSize() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        assertThat(linesDataSet.getXAxisSize()).isEqualTo(0);

        Map<String, Integer>  dataSetSeries = new HashMap<>();
        linesDataSet.add(TESTLABEL, dataSetSeries);
        assertThat(linesDataSet.getXAxisSize()).isEqualTo(1);
        assertThat(linesDataSet.getXAxisLabels()).containsOnly("Testlabel");
    }

    @Test
    void shouldGetDataSetIds() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        Map<String, Integer>  dataSetSeries = getdataSetSeriesMap();
        linesDataSet.add(TESTLABEL, dataSetSeries);
        assertThat(linesDataSet.getDataSetIds())
                .hasSize(4)
                .contains("Eins")
                .contains("Zwei")
                .contains("Drei")
                .contains("Vier");
    }

    @Test
    void shouldHasSeries() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        assertThat(linesDataSet.hasSeries("Eins")).isEqualTo(false);
        Map<String, Integer>  dataSetSeries = getdataSetSeriesMap();
        linesDataSet.add(TESTLABEL, dataSetSeries);
        assertThat(linesDataSet.hasSeries("Eins")).isEqualTo(true);
        assertThat(linesDataSet.getSeries("Eins")).isEqualTo(Collections.singletonList(1));
    }

    @Test
    void shouldGetSeriesWithMissingEntry() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        assertThat(linesDataSet.hasSeries("Eins")).isEqualTo(false);
        Map<String, Integer>  dataSetSeries = getdataSetSeriesMap();
        linesDataSet.add(TESTLABEL, dataSetSeries);

        assertThatThrownBy(() -> linesDataSet.getSeries("Test"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("No dataset")
                .hasMessageContaining("registered");
    }

    @Test
    void shouldAddWithException() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        assertThat(linesDataSet.hasSeries("Eins")).isEqualTo(false);
        Map<String, Integer>  dataSetSeries = Collections.emptyMap();
        linesDataSet.add(TESTLABEL, dataSetSeries);

        assertThatThrownBy(() -> linesDataSet.add("Testlabel", dataSetSeries))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Label already registered:");
    }

}