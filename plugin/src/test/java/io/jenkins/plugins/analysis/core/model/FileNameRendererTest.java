package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link FileNameRenderer}.
 */
@SuppressFBWarnings("DMI")
class FileNameRendererTest {
    @Test
    void shouldExtractBaseName() {
        FileNameRenderer renderer = createRenderer(true);

        Issue fileIssue = new IssueBuilder().setFileName("/path/to/affected/file.txt").setLineStart(20).build();

        assertThat(renderer.getFileName(fileIssue)).isEqualTo("file.txt");
        assertThat(renderer.getFileNameAtLine(fileIssue)).isEqualTo("file.txt:20");

        Issue consoleIssue = new IssueBuilder().setFileName(ConsoleLogHandler.JENKINS_CONSOLE_LOG_FILE_NAME_ID).setLineStart(20).build();

        assertThat(renderer.getFileName(consoleIssue)).isEqualTo(Messages.ConsoleLog_Name());
        assertThat(renderer.getFileNameAtLine(consoleIssue)).isEqualTo(Messages.ConsoleLog_Name() + ":20");
    }

    @Test
    void shouldCreateFileNameAsLink() {
        FileNameRenderer renderer = createRenderer(true);

        Issue issue = new IssueBuilder().setFileName("/path/to/affected/file.txt").setLineStart(20).build();

        assertThat(renderer.renderAffectedFileLink(issue)).matches("<a href=\"source\\.[0-9a-f-]+/#20\" data-toggle=\"tooltip\" data-placement=\"bottom\" title=\"/path/to/affected/file.txt\">file.txt:20</a>");
        assertThat(renderer.renderAffectedFileLink(issue)).contains(issue.getId().toString());
    }

    private BuildFolderFacade createBuildFolderStub(final boolean isAccessible) {
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.canAccessAffectedFileOf(any(), any())).thenReturn(isAccessible);
        return buildFolder;
    }

    @Test
    void shouldCreateFileNameAsPlainText() {
        FileNameRenderer renderer = createRenderer(false);

        Issue issue = new IssueBuilder().setFileName("/path/to/affected/file.txt").setLineStart(20).build();
        assertThat(renderer.renderAffectedFileLink(issue)).isEqualTo("file.txt:20");

        Issue another = new IssueBuilder().setFileName("/private/tmp/node2/workspace/New - Freestyle - Model/src/main/java/edu/hm/hafner/analysis/parser/FindBugsParser.java").setLineStart(1).build();
        assertThat(renderer.renderAffectedFileLink(another)).isEqualTo("FindBugsParser.java:1");
        assertThat(renderer.getFileName(another)).isEqualTo("FindBugsParser.java");
        assertThat(renderer.getFileNameAtLine(another)).isEqualTo("FindBugsParser.java:1");
        assertThat(renderer.getSourceCodeUrl(another)).contains("#1").contains(another.getId().toString());
    }

    @Test
    void shouldCreateLinkToConsoleLog() {
        FileNameRenderer renderer = createRenderer(false);

        Issue issue = new IssueBuilder().setFileName(ConsoleLogHandler.JENKINS_CONSOLE_LOG_FILE_NAME_ID).setLineStart(20).build();
        assertThat(renderer.renderAffectedFileLink(issue)).matches("<a href=\"source\\.[0-9a-f-]+/#20\">Console Output:20</a>");
        assertThat(renderer.renderAffectedFileLink(issue)).contains(issue.getId().toString());
    }

    private FileNameRenderer createRenderer(final boolean isAccessible) {
        return new FileNameRenderer(createBuildStub(), createBuildFolderStub(isAccessible));
    }

    @Nullable
    private Run<?, ?> createBuildStub() {
        return mock(Run.class);
    }
}
