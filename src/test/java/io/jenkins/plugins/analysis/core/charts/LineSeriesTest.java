package io.jenkins.plugins.analysis.core.charts;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.charts.LineSeries.FilledMode;
import io.jenkins.plugins.analysis.core.charts.LineSeries.StackedMode;
import io.jenkins.plugins.analysis.core.util.JacksonFacade;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;

/**
 * Tests the class {@link LineSeries}.
 *
 * @author Ullrich Hafner
 */
class LineSeriesTest {
    private static final String SEVERITY = "High";
    private static final String STACKED = "stacked";
    private static final String LINE = "line";
    private static final String COLOR = "#fff";

    @Test
    void shouldCreateLineSeriesStackedLines() {
        String lineSeries = createLineSeries(StackedMode.STACKED, FilledMode.LINES);

        assertThatJson(lineSeries).node("areaStyle").isNull();
        assertThatJson(lineSeries).node("stack").isEqualTo(STACKED);
        assertThatOtherPropertiesAreCorrectlySet(lineSeries);
    }

    private void assertThatOtherPropertiesAreCorrectlySet(final String lineSeries) {
        assertThatJson(lineSeries).node("name").isEqualTo(SEVERITY);
        assertThatJson(lineSeries).node("type").isEqualTo(LINE);
        assertThatJson(lineSeries).node("data").isArray().hasSize(0);
    }

    @Test
    void shouldCreateLineSeriesLines() {
        String lineSeries = createLineSeries(StackedMode.SEPARATE_LINES, FilledMode.LINES);

        assertThatJson(lineSeries).node("areaStyle").isNull();
        assertThatJson(lineSeries).node("stack").isEqualTo(StringUtils.EMPTY);
        assertThatOtherPropertiesAreCorrectlySet(lineSeries);
    }

    @Test
    void shouldCreateLineSeriesStackedFilled() {
        String lineSeries = createLineSeries(StackedMode.STACKED, FilledMode.FILLED);

        assertThatJson(lineSeries).node("areaStyle").isEqualTo(new AreaStyle());
        assertThatJson(lineSeries).node("stack").isEqualTo(STACKED);
        assertThatOtherPropertiesAreCorrectlySet(lineSeries);
    }

    @Test
    void shouldCreateLineSeriesLinesFilled() {
        String lineSeries = createLineSeries(StackedMode.SEPARATE_LINES, FilledMode.FILLED);

        assertThatJson(lineSeries).node("areaStyle").isEqualTo(new AreaStyle());
        assertThatJson(lineSeries).node("stack").isEqualTo(StringUtils.EMPTY);
        assertThatOtherPropertiesAreCorrectlySet(lineSeries);
    }

    @Test
    void shouldCreateLineSeriesWithValues() {
        LineSeries lineSeries = new LineSeries(SEVERITY, COLOR, StackedMode.STACKED, FilledMode.LINES);
        lineSeries.add(22);

        assertThatJson(lineSeries).node("data").isArray().hasSize(1).contains(22);
    }

    private String createLineSeries(final StackedMode stacked, final FilledMode lines) {
        return new JacksonFacade().toJson(new LineSeries(SEVERITY, COLOR, stacked, lines));
    }
}