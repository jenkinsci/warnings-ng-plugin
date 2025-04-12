package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link FixedWarningsDetail}.
 *
 * @author Ullrich Hafner
 */
class FixedWarningsDetailTest {
    private static final Issue ISSUE = new IssueBuilder().build();

    @Test
    void shouldDisplayFileOfFixedWarning() {
        var result = createAnalysisResult();

        var detail = new FixedWarningsDetail(mock(Run.class), result, new Report(),
                "fixed", mock(StaticAnalysisLabelProvider.class), StandardCharsets.UTF_8);

        // No reference build yet
        assertThat(detail.canDisplayFile(ISSUE)).isFalse();
        assertThat(detail.getReferenceUrl()).isEmpty();

        // Reference build exists, but affected file does not exist
        Run<?, ?> referenceBuild = createReferenceBuild();
        when(result.getReferenceBuild()).thenReturn(Optional.of(referenceBuild));

        var expectedUrl = "/url/analysis";
        assertThat(detail.getReferenceUrl()).isEqualTo(expectedUrl);
        assertThat(detail.canDisplayFile(createIssue("file.txt"))).isFalse();

        assertThat(detail.getReferenceUrl()).isEqualTo(expectedUrl);
        assertThat(detail.canDisplayFile(createIssue(ConsoleLogHandler.JENKINS_CONSOLE_LOG_FILE_NAME_ID))).isTrue();
    }

    private Issue createIssue(final String fileName) {
        try (var builder = new IssueBuilder()) {
            builder.setFileName(fileName);
            return builder.build();
        }
    }

    private Run<?, ?> createReferenceBuild() {
        Run<?, ?> referenceBuild = mock(Run.class);
        when(referenceBuild.getUrl()).thenReturn("/url/");
        when(referenceBuild.getRootDir()).thenReturn(new File(""));
        return referenceBuild;
    }

    private AnalysisResult createAnalysisResult() {
        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getId()).thenReturn("analysis");
        return result;
    }
}
