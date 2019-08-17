package io.jenkins.plugins.analysis.core.model;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.IssuesModel.IssuesRow;

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
        List<Object> rows = new ArrayList<>();
        rows.add(new IssuesRow());
        rows.add(new IssuesRow());

        when(model.getContent(any())).thenReturn(rows);
        when(labelProvider.getIssuesModel(run, "url")).thenReturn(model);

        assertThatJson(detail.getTableModel("#issues"))
                .isArray().hasSize(2)
                .isEqualTo("["
                        + "{\"description\":null,\"fileName\":null,\"packageName\":null,\"category\":null,\"type\":null,\"severity\":null,\"age\":null},"
                        + "{\"description\":null,\"fileName\":null,\"packageName\":null,\"category\":null,\"type\":null,\"severity\":null,\"age\":null}"
                        + "]");
    }
}
