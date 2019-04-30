package io.jenkins.plugins.analysis.core.model;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;

import hudson.model.Run;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link IssuesDetail}.
 *
 * @author Ullrich Hafner
 */
class IssuesDetailTest {
    @Test
    void shouldConvertIssuesTable() {
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        Run run = mock(Run.class);
        IssuesDetail detail = new IssuesDetail(run, mock(AnalysisResult.class), new Report(), new Report(),
                new Report(), new Report(), "name", "url",
                labelProvider, StandardCharsets.UTF_8);

        DetailsTableModel model = mock(DetailsTableModel.class);
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("1Col1", "1Col2", "1Col3"));
        rows.add(Arrays.asList("2Col1", "2Col2", "2Col3"));

        when(model.getContent(any())).thenReturn(rows);
        when(labelProvider.getIssuesModel(run, "url")).thenReturn(model);

        assertThatJson(detail.getTableModel("#issues")).node("data")
                .isArray()
                .isEqualTo("[[\"1Col1\",\"1Col2\",\"1Col3\"],[\"2Col1\",\"2Col2\",\"2Col3\"]]");
    }
}