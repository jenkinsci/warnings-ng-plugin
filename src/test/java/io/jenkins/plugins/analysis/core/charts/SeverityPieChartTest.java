package io.jenkins.plugins.analysis.core.charts;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link SeverityPieChart}
 * @author Matthias Herpers
 */
class SeverityPieChartTest {
    /**
     *  Verifies that a SeverityPieChart with empty report shows only predefined severities without Severity.ERROR.
     */
    @Test
    void shouldCreateChartFromEmptyReport() {
        SeverityPieChart severityPieChart = new SeverityPieChart();
        Report report = createReport(0, 0, 0, 0);
        PieChartModel pieChartModel = severityPieChart.create(report);
        List<PieData> data = pieChartModel.getData();
        assertThat(data.get(0).getName()).isEqualTo("High");// Without Severity.ERROR
        assertThat(data.get(0).getValue()).isEqualTo(0);
        assertThat(data.get(1).getName()).isEqualTo("Normal");
        assertThat(data.get(1).getValue()).isEqualTo(0);
        assertThat(data.get(2).getName()).isEqualTo("Low");
        assertThat(data.get(2).getValue()).isEqualTo(0);
    }

    /**
     *  Verifies that a SeverityPieChart with one Error shows all Severities.
     */
    @Test
    void shouldCreateChartFromReportWithOneError() {
        SeverityPieChart severityPieChart = new SeverityPieChart();
        Report report = createReport(0, 0, 0, 1);
        PieChartModel pieChartModel = severityPieChart.create(report);
        List<PieData> data = pieChartModel.getData();
        assertThat(data.get(0).getName()).isEqualTo("Error");// With Severity.ERROR
        assertThat(data.get(0).getValue()).isEqualTo(1);
        assertThat(data.get(1).getName()).isEqualTo("High");
        assertThat(data.get(1).getValue()).isEqualTo(0);
        assertThat(data.get(2).getName()).isEqualTo("Normal");
        assertThat(data.get(2).getValue()).isEqualTo(0);
        assertThat(data.get(3).getName()).isEqualTo("Low");
        assertThat(data.get(3).getValue()).isEqualTo(0);
    }

    /**
     *  Verifies that a SeverityPieChart with one of each severity shows all Severities.
     */
    @Test
    void shouldCreateChartFromSimpleReport() {
        SeverityPieChart severityPieChart = new SeverityPieChart();
        Report report = createReport(1, 1, 1, 1);
        PieChartModel pieChartModel = severityPieChart.create(report);
        List<PieData> data = pieChartModel.getData();
        assertThat(data.get(0).getName()).isEqualTo("Error");// With Severity.ERROR
        assertThat(data.get(0).getValue()).isEqualTo(1);
        assertThat(data.get(1).getName()).isEqualTo("High");
        assertThat(data.get(1).getValue()).isEqualTo(1);
        assertThat(data.get(2).getName()).isEqualTo("Normal");
        assertThat(data.get(2).getValue()).isEqualTo(1);
        assertThat(data.get(3).getName()).isEqualTo("Low");
        assertThat(data.get(3).getValue()).isEqualTo(1);
    }

    private Report createReport(final int high, final int normal, final int low, final int error) {
        Report buildResult = mock(Report.class);
        when(buildResult.getSizeOf(Severity.WARNING_HIGH)).thenReturn(high);
        when(buildResult.getSizeOf(Severity.WARNING_NORMAL)).thenReturn(normal);
        when(buildResult.getSizeOf(Severity.WARNING_LOW)).thenReturn(low);
        when(buildResult.getSizeOf(Severity.ERROR)).thenReturn(error);
        return buildResult;
    }
}
