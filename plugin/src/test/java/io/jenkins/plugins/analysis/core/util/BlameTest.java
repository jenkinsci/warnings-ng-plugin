package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;

import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileBlame;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link Blame}.
 *
 * @author Ullrich Hafner
 */
class BlameTest {
    private static final String COMMIT = "Commit";
    private static final String AUTHOR = "Author";
    private static final String EMAIL = "Email";
    private static final int ADDED_AT = 123;

    @Test
    void shouldCreateEmptyBlame() {
        var blame = new Blame(new IssueBuilder().build(), new Blames());

        assertThat(blame).hasAuthorName(Blame.UNDEFINED);
        assertThat(blame).hasCommit(Blame.UNDEFINED);
        assertThat(blame).hasAuthorEmail(Blame.UNDEFINED);
        assertThat(blame).hasAddedAt(Blame.UNDEFINED_DATE);
    }

    @Test
    void shouldCreateBlameForIssue() {
        var blame = new Blame(new IssueBuilder().build(), createBlames());

        assertThat(blame).hasAuthorName(AUTHOR);
        assertThat(blame).hasCommit(COMMIT);
        assertThat(blame).hasAuthorEmail(EMAIL);
        assertThat(blame).hasAddedAt(ADDED_AT);
    }

    private Blames createBlames() {
        Blames blames = mock(Blames.class);
        FileBlame fileBlame = mock(FileBlame.class);
        when(fileBlame.getCommit(anyInt())).thenReturn(COMMIT);
        when(fileBlame.getEmail(anyInt())).thenReturn(EMAIL);
        when(fileBlame.getName(anyInt())).thenReturn(AUTHOR);
        when(fileBlame.getTime(anyInt())).thenReturn(ADDED_AT);

        when(blames.getBlame(anyString())).thenReturn(fileBlame);
        when(blames.contains(anyString())).thenReturn(true);

        return blames;
    }
}
