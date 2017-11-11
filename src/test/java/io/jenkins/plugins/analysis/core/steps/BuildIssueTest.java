package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link BuildIssue}.
 *
 * @author Ullrich Hafner
 */
class BuildIssueTest {
    @Test
    void shouldUseIdOfWrappedElement() {
        IssueBuilder builder = new IssueBuilder();
        Issue emptyIssue = builder.build();
        int build = 1;
        BuildIssue issue = new BuildIssue(emptyIssue, build);

        assertThat(issue.getBuild()).isEqualTo(build);
        assertThat(issue.getId()).isEqualTo(emptyIssue.getId());
    }
}