package io.jenkins.plugins.analysis.core.charts;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.PieData;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link SeverityPieChart}.
 *
 * @author Matthias Herpers
 */
class SeverityPieChartTest {
    /**
     *  Verifies that a SeverityPieChart with empty report shows only predefined severities without Severity.ERROR.
     */
    @Test
    void shouldCreateChartFromEmptyReport() {
        var severityPieChart = new SeverityPieChart();

        var pieChartModel = severityPieChart.create(createReport(0, 0, 0, 0));
        List<PieData> data = pieChartModel.getData();

        assertThat(data.get(0)).isEqualTo(new PieData("High", 0));
        assertThat(data.get(1)).isEqualTo(new PieData("Normal", 0));
        assertThat(data.get(2)).isEqualTo(new PieData("Low", 0));
    }

    /**
     *  Verifies that a SeverityPieChart with one Error shows all Severities.
     */
    @Test
    void shouldCreateChartFromReportWithOneError() {
        var severityPieChart = new SeverityPieChart();

        var pieChartModel = severityPieChart.create(createReport(1, 0, 0, 0));
        List<PieData> data = pieChartModel.getData();

        assertThat(data.get(0)).isEqualTo(new PieData("Error", 1));
        assertThat(data.get(1)).isEqualTo(new PieData("High", 0));
        assertThat(data.get(2)).isEqualTo(new PieData("Normal", 0));
        assertThat(data.get(3)).isEqualTo(new PieData("Low", 0));
    }

    /**
     *  Verifies that a SeverityPieChart with one of each severity shows all Severities.
     */
    @Test
    void shouldCreateChartFromSimpleReport() {
        var severityPieChart = new SeverityPieChart();

        var pieChartModel = severityPieChart.create(createReport(1, 1, 1, 1));
        List<PieData> data = pieChartModel.getData();

        assertThat(data.get(0)).isEqualTo(new PieData("Error", 1));
        assertThat(data.get(1)).isEqualTo(new PieData("High", 1));
        assertThat(data.get(2)).isEqualTo(new PieData("Normal", 1));
        assertThat(data.get(3)).isEqualTo(new PieData("Low", 1));
    }

    private Report createReport(final int error, final int high, final int normal, final int low) {
        Report buildResult = mock(Report.class);
        when(buildResult.getSizeOf(Severity.ERROR)).thenReturn(error);
        when(buildResult.getSizeOf(Severity.WARNING_HIGH)).thenReturn(high);
        when(buildResult.getSizeOf(Severity.WARNING_NORMAL)).thenReturn(normal);
        when(buildResult.getSizeOf(Severity.WARNING_LOW)).thenReturn(low);
        return buildResult;
    }
}
