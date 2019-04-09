package io.jenkins.plugins.analysis.core.charts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.*;

/**
 * Testclass for LinesDataSet.
 *
 * @author Lorenz Munsch
 */
class LinesDataSetTest {


    @Test
    void getXAxisSize() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        assertThat(linesDataSet.getXAxisSize()).isEqualTo(0);

        Map<String, Integer>  dataSetSeries = new HashMap<>();
        linesDataSet.add("Testlabel", dataSetSeries);
        assertThat(linesDataSet.getXAxisSize()).isEqualTo(1);
    }

    @Test
    void getXAxisLabels() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        Map<String, Integer>  dataSetSeries = new HashMap<>();
        linesDataSet.add("Testlabel", dataSetSeries);
        assertThat(linesDataSet.getXAxisLabels()).isEqualTo(Collections.singletonList("Testlabel"));
    }

    @Test
    void getDataSetIds() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        Map<String, Integer>  dataSetSeries = new HashMap<>();
        dataSetSeries.put("Eins", 1);
        dataSetSeries.put("Zwei", 2);
        dataSetSeries.put("Drei", 3);
        dataSetSeries.put("Vier", 4);
        linesDataSet.add("Testlabel", dataSetSeries);
        assertThat(linesDataSet.getDataSetIds().size()).isEqualTo(4);
        assertThat(linesDataSet.getDataSetIds().contains("Eins")).isEqualTo(true);
        assertThat(linesDataSet.getDataSetIds().contains("Zwei")).isEqualTo(true);
        assertThat(linesDataSet.getDataSetIds().contains("Drei")).isEqualTo(true);
        assertThat(linesDataSet.getDataSetIds().contains("Vier")).isEqualTo(true);
        assertThat(linesDataSet.getDataSetIds().contains("Fuenf")).isEqualTo(false);
    }

    @Test
    void hasSeries() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        assertThat(linesDataSet.hasSeries("Eins")).isEqualTo(false);
        Map<String, Integer>  dataSetSeries = new HashMap<>();
        dataSetSeries.put("Eins", 1);
        dataSetSeries.put("Zwei", 2);
        dataSetSeries.put("Drei", 3);
        dataSetSeries.put("Vier", 4);
        linesDataSet.add("Testlabel", dataSetSeries);
        assertThat(linesDataSet.hasSeries("Eins")).isEqualTo(true);
    }

    @Test
    void getSeries() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        assertThat(linesDataSet.hasSeries("Eins")).isEqualTo(false);
        Map<String, Integer>  dataSetSeries = new HashMap<>();
        dataSetSeries.put("Eins", 1);
        dataSetSeries.put("Zwei", 2);
        dataSetSeries.put("Drei", 3);
        dataSetSeries.put("Vier", 4);
        linesDataSet.add("Testlabel", dataSetSeries);
        assertThat(linesDataSet.getSeries("Eins")).isEqualTo(Collections.singletonList(1));
    }

    @Test
    void getSeriesWithMissingEntry() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        assertThat(linesDataSet.hasSeries("Eins")).isEqualTo(false);
        Map<String, Integer>  dataSetSeries = new HashMap<>();
        dataSetSeries.put("Eins", 1);
        dataSetSeries.put("Zwei", 2);
        dataSetSeries.put("Drei", 3);
        dataSetSeries.put("Vier", 4);
        linesDataSet.add("Testlabel", dataSetSeries);

        Assertions.assertThrows(AssertionError.class, () -> linesDataSet.getSeries("Test"));
    }

    @Test
    void addWithException() {
        LinesDataSet linesDataSet  = new LinesDataSet();
        assertThat(linesDataSet.hasSeries("Eins")).isEqualTo(false);
        Map<String, Integer>  dataSetSeries = new HashMap<>();
        linesDataSet.add("Testlabel", dataSetSeries);

        Assertions.assertThrows(IllegalStateException.class, () -> linesDataSet.add("Testlabel", dataSetSeries));
    }

}