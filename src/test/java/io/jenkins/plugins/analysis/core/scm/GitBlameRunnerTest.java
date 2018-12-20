package io.jenkins.plugins.analysis.core.scm;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.scm.GitBlamer.BlameCallback;
import io.jenkins.plugins.analysis.core.scm.GitBlamer.BlameRunner;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link GitBlamer}.
 *
 * @author Ullrich Hafner
 */
class GitBlameRunnerTest {
    private static final String EMAIL = "email";
    private static final String NAME = "name";
    private static final String WORKSPACE = "workspace";

    @Test @Issue("JENKINS-55273")
    void shouldNotFailOnExceptions() throws GitAPIException {
        verifyExceptionHandling(NoHeadException.class);
        verifyExceptionHandling(JGitInternalException.class);
    }

    private void verifyExceptionHandling(final Class<? extends Exception> exception) throws GitAPIException {
        Report report = new Report();

        BlameCallback callback = new BlameCallback(report, mock(ObjectId.class), WORKSPACE);

        BlameRequest request = new BlameRequest("exception", 1);
        BlameRunner runner = mock(BlameRunner.class);
        when(runner.run("exception")).thenThrow(exception);
        callback.run(request, runner);

        assertThat(report.getErrorMessages()).hasSize(3);
        assertThat(report.getErrorMessages().get(1)).startsWith("- error running git blame on 'exception' with revision");
        assertThat(report.getErrorMessages().get(2)).startsWith(exception.getName());
    }

    @Test
    void shouldMapResultToRequestWithOneLine() throws GitAPIException {
        BlameCallback callback = new BlameCallback(new Report(), mock(ObjectId.class), WORKSPACE);

        BlameResult result = createResult(1);

        createResultForLine(result, 0);

        BlameRunner blameRunner = createBlameRunner(result);
        BlameRequest request = new BlameRequest("file", 1);
        callback.run(request, blameRunner);

        verifyResult(request, 1);
    }

    @Test
    void shouldMapResultToRequestWithTwoLines() throws GitAPIException {
        BlameCallback callback = new BlameCallback(new Report(), mock(ObjectId.class), WORKSPACE);

        BlameResult result = createResult(2);

        createResultForLine(result, 0);
        createResultForLine(result, 1);

        BlameRunner blameRunner = createBlameRunner(result);
        BlameRequest request = new BlameRequest("file", 1).addLineNumber(2);
        callback.run(request, blameRunner);

        verifyResult(request, 1);
        verifyResult(request, 2);
    }

    @Test
    void shouldMapResultToRequestOutOfRange() throws GitAPIException {
        BlameCallback callback = new BlameCallback(new Report(), mock(ObjectId.class), WORKSPACE);

        BlameResult result = createResult(2);

        createResultForLine(result, 2);

        BlameRunner blameRunner = createBlameRunner(result);
        BlameRequest request = new BlameRequest("file", 3);
        callback.run(request, blameRunner);

        assertThat(request.getEmail(3)).isEqualTo("-");
        assertThat(request.getName(3)).isEqualTo("-");
        assertThat(request.getCommit(3)).isEqualTo("-");
    }

    private BlameResult createResult(final int size) {
        RawText resultSize = createResultSize(size);
        BlameResult result = mock(BlameResult.class);
        when(result.getResultContents()).thenReturn(resultSize);
        return result;
    }

    private BlameRunner createBlameRunner(final BlameResult result) throws GitAPIException {
        BlameRunner blameRunner = mock(BlameRunner.class);
        when(blameRunner.run("file")).thenReturn(result);
        return blameRunner;
    }

    private RawText createResultSize(final int size) {
        RawText text = mock(RawText.class);
        when(text.size()).thenReturn(size);
        return text;
    }

    private void createResultForLine(final BlameResult result, final int index) {
        int line = index + 1;
        when(result.getSourceAuthor(index)).thenReturn(new PersonIdent(NAME + line, EMAIL + line));
        RevCommit commit = mock(RevCommit.class);
        when(result.getSourceCommit(index)).thenReturn(commit);
    }

    private void verifyResult(final BlameRequest request, final int line) {
        assertThat(request.getEmail(line)).isEqualTo(EMAIL + line);
        assertThat(request.getName(line)).isEqualTo(NAME + line);
        assertThat(request.getCommit(line)).isNotBlank(); // final getter
    }
}