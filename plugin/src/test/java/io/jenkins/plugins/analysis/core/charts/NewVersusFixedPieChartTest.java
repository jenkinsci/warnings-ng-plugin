package io.jenkins.plugins.analysis.core.charts;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.echarts.PieChartModel;
import edu.hm.hafner.echarts.PieData;

import static io.jenkins.plugins.analysis.core.charts.Messages.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ModifiedCodePieChart}.
 *
 * @author Veronika Zwickenpflug
 */
class NewVersusFixedPieChartTest {
    @Test
    void testCreate() {
        NewVersusFixedPieChart chart = new NewVersusFixedPieChart();

        PieChartModel model = chart.create(createReportWithSize(2), createReportWithSize(3), createReportWithSize(4));

        assertThat(model.getData()).map(PieData::getName).containsExactly(
                New_Warnings_Short(), Outstanding_Warnings_Short(), Fixed_Warnings_Short());
        assertThat(model.getData()).map(PieData::getValue).containsExactly(
                2, 3, 4);
    }

    private Report createReportWithSize(final int size) {
        Report report = mock(Report.class);
        when(report.size()).thenReturn(size);
        return report;
    }
}
