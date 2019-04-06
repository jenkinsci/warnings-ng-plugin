package io.jenkins.plugins.analysis.core.charts;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link SeverityPieChart}.
 *
 * @author Matthias Herpers
 */
class SeverityPieChartTest {

    /** First Dummy test to test building. */
    @Test
    void buildTest() {

        SeverityPieChart severityPieChart = new SeverityPieChart();
        int numberOfDefaultPieData = Severity.getPredefinedValues().size() - 1; // Without Severity.ERROR
        //Create Report
        Report report = new Report();
        //pass Report to SeverityPieChart
        PieChartModel pieChartModel = severityPieChart.create(report);
        List<PieData> data = pieChartModel.getData();
        //Assert
        assertThat(data.size()).isEqualTo(numberOfDefaultPieData);
    }
}
