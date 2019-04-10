package io.jenkins.plugins.analysis.core.charts;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

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
    void ShouldCreateChartFromEmptyReport() {

        SeverityPieChart severityPieChart = new SeverityPieChart();
        int numberOfDefaultPieData = Severity.getPredefinedValues().size() - 1; // Without Severity.ERROR
        Report report = createReport(0, 0, 0, 0);
        PieChartModel pieChartModel = severityPieChart.create(report);
        List<PieData> data = pieChartModel.getData();
        for (PieData severity : data) {
            assertThat(Severity.getPredefinedValues().contains(severity));
        }

        assertThat(data.size()).isEqualTo(numberOfDefaultPieData);
    }


    /**
     *  Verifies that a SeverityPieChart with one Error shows all Severities.
     */
    @Test
    void ShouldCreateChartFromReportWithOneError() {

        SeverityPieChart severityPieChart = new SeverityPieChart();
        int numberOfDefaultPieData = Severity.getPredefinedValues().size(); // With Severity.ERROR
        Report report = createReport(0, 0, 0, 1);
        PieChartModel pieChartModel = severityPieChart.create(report);
        List<PieData> data = pieChartModel.getData();
        for (PieData severity : data) {
            assertThat(Severity.getPredefinedValues().contains(severity));
        }

        assertThat(data.size()).isEqualTo(numberOfDefaultPieData);
    }


    /**
     *  Verifies that a SeverityPieChart with one of each severity shows all Severities.
     */
    @Test
    void ShouldCreateChartFromSimpleReport() {

        SeverityPieChart severityPieChart = new SeverityPieChart();
        int numberOfDefaultPieData = Severity.getPredefinedValues().size(); // With Severity.ERROR
        Report report = createReport(1, 1, 1, 1);
        PieChartModel pieChartModel = severityPieChart.create(report);
        List<PieData> data = pieChartModel.getData();
        for (PieData severity : data) {
            assertThat(Severity.getPredefinedValues().contains(severity));
        }

        assertThat(data.size()).isEqualTo(numberOfDefaultPieData);
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
