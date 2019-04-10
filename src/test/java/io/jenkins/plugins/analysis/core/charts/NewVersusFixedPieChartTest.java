package io.jenkins.plugins.analysis.core.charts;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;

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
        final int[] sizes = new int[] {2, 3, 4};
        final String[] names = new String[] {
                Messages.New_Warnings_Short(), Messages.Outstanding_Warnings_Short(), Messages.Fixed_Warnings_Short()};
        final Palette[] colors = new Palette[] {
                Palette.RED, Palette.YELLOW, Palette.GREEN};

        NewVersusFixedPieChart chart = new NewVersusFixedPieChart();

        PieChartModel model = chart.create(mockReport(sizes[0]), mockReport(sizes[1]), mockReport(sizes[2]));
        List<PieData> data = model.getData();
        assertThat(model.getData().size()).isEqualTo(3);
        assertThat(model.getColors().size()).isEqualTo(3);

        for (int i = 0; i < 3; i++) {
            assertThat(data.get(i).getName()).isEqualTo(names[i]);
            assertThat(data.get(i).getValue()).isEqualTo(sizes[i]);
            assertThat(model.getColors().get(i)).isEqualTo(colors[i].getNormal());
        }
    }

    private Report mockReport(final int size) {
        Report report = mock(Report.class);
        when(report.size()).thenReturn(size);
        return report;
    }
}
