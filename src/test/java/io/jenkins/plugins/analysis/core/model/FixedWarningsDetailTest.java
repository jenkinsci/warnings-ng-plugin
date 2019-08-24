package io.jenkins.plugins.analysis.core.model;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

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
    @Test
    void shouldNotDisplayFile() {
        Report fixedIssues = new Report();

        Issue issue = new IssueBuilder().build();
        fixedIssues.add(issue);

        AnalysisResult result = mock(AnalysisResult.class);
        FixedWarningsDetail detail = new FixedWarningsDetail(mock(Run.class), result, fixedIssues,
                "fixed", mock(StaticAnalysisLabelProvider.class), StandardCharsets.UTF_8);

        assertThat(detail.canDisplayFile(issue)).isFalse();

        Run<?, ?> referenceBuild = mock(Run.class);
        when(referenceBuild.getRootDir()).thenReturn(new File(""));
        when(result.getReferenceBuild()).thenReturn(Optional.of(referenceBuild));

        assertThat(detail.canDisplayFile(issue)).isFalse();
    }

    @Test
    void shouldDisplayFile() {
        Report fixedIssues = new Report();

        IssueBuilder builder = new IssueBuilder();
        builder.setFileName(ConsoleLogHandler.JENKINS_CONSOLE_LOG_FILE_NAME_ID);
        Issue issue = builder.build();
        fixedIssues.add(issue);

        Run<?, ?> run = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);

        Run<?, ?> referenceBuild = mock(Run.class);
        when(referenceBuild.getRootDir()).thenReturn(new File(""));
        when(result.getReferenceBuild()).thenReturn(Optional.of(referenceBuild));

        FixedWarningsDetail detail = new FixedWarningsDetail(run, result, fixedIssues,
                "fixed", mock(StaticAnalysisLabelProvider.class), StandardCharsets.UTF_8);

        assertThat(detail.canDisplayFile(builder.build())).isTrue();
    }
}
