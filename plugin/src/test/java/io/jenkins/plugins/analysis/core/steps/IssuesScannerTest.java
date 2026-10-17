package io.jenkins.plugins.analysis.core.steps;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.Report;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.analysis.core.filter.FilterConfig;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.prism.SourceCodeRetention;
import io.jenkins.plugins.util.LogHandler;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link IssuesScanner}.
 */
class IssuesScannerTest {
    @Test
    void shouldDelegateScanWithNullFlowNodeWhenConstructedWithoutFlowNode() throws IOException, InterruptedException {
        var tool = mock(Tool.class);
        var descriptor = mock(Tool.ToolDescriptor.class);
        when(descriptor.isPostProcessingEnabled()).thenReturn(false);
        when(tool.getDescriptor()).thenReturn(descriptor);
        when(tool.getActualName()).thenReturn("tool");
        when(tool.getActualId()).thenReturn("tool-id");
        when(tool.scan(any(), any(), any(), any(), any())).thenReturn(new Report());

        var filterConfig = new FilterConfig(Collections.emptyList(), "");
        var workspace = new FilePath(new File("."));
        var jenkinsRootDir = new FilePath(new File("."));
        var run = mock(Run.class);
        var listener = mock(TaskListener.class);

        var scanner = new IssuesScanner(
                tool,
                filterConfig,
                StandardCharsets.UTF_8,
                workspace,
                Collections.emptySet(),
                SourceCodeRetention.NEVER,
                run,
                jenkinsRootDir,
                listener,
                "",
                IssuesScanner.BlameMode.DISABLED,
                IssuesScanner.PostProcessingMode.DISABLED,
                true,
                "",
                "");

        var report = scanner.scan();

        assertThat(report).isNotNull();
        verify(tool).scan(eq(run), isNull(), eq(workspace), eq(StandardCharsets.UTF_8), any(LogHandler.class));
    }
}
