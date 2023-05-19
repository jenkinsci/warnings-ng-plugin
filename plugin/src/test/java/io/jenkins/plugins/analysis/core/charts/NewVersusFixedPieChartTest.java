package io.jenkins.plugins.analysis.core.charts;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.echarts.PieChartModel;
import edu.hm.hafner.echarts.PieData;

import io.jenkins.plugins.echarts.JenkinsPalette;

import static io.jenkins.plugins.analysis.core.charts.Messages.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link NewVersusFixedPieChart}.
 *
 * @author Veronika Zwickenpflug
 */
class NewVersusFixedPieChartTest {
    @Test
    void testCreate() {
        int[] sizes = {2, 3, 4};
        String[] names = {New_Warnings_Short(), Outstanding_Warnings_Short(), Fixed_Warnings_Short()};
        JenkinsPalette[] colors = {JenkinsPalette.RED, JenkinsPalette.YELLOW, JenkinsPalette.GREEN};

        NewVersusFixedPieChart chart = new NewVersusFixedPieChart();

        PieChartModel model = chart.create(createReportStub(sizes[0]), createReportStub(sizes[1]), createReportStub(sizes[2]));
        List<PieData> data = model.getData();
        assertThat(model.getData().size()).isEqualTo(3);
        assertThat(model.getColors().size()).isEqualTo(3);

        for (int i = 0; i < 3; i++) {
            assertThat(data.get(i).getName()).isEqualTo(names[i]);
            assertThat(data.get(i).getValue()).isEqualTo(sizes[i]);
            assertThat(model.getColors().get(i)).isEqualTo(colors[i].normal());
        }
    }

    private Report createReportStub(final int size) {
        Report report = mock(Report.class);
        when(report.size()).thenReturn(size);
        return report;
    }
}
