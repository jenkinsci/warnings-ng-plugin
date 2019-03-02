package io.jenkins.plugins.analysis.core.scm;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.scm.BlameRequest.EMPTY;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link BlameRequest}.
 *
 * @author Ullrich Hafner
 */
class BlameRequestTest {
    private static final String COMMIT = "commit";
    private static final String NAME = "name";
    private static final String EMAIL = "email";

    @Test
    void shouldCreateRequest() {
        BlameRequest request = new BlameRequest("file", 15);

        assertThat(request).hasSize(1).containsExactly(15);
        assertThat(request.getFileName()).isEqualTo("file");
        assertThat(request).isEqualTo(new BlameRequest("file", 15));

        setDetails(request, 15);
        verifyDetails(request, 15);
        BlameRequest other = new BlameRequest("file", 15);
        setDetails(other, 15);

        assertThat(request).isEqualTo(other);

        request.addLineNumber(25);
        assertThat(request).hasSize(2).containsExactlyInAnyOrder(15, 25);

        setDetails(request, 25);
        verifyDetails(request, 25);
    }

    private void verifyDetails(final BlameRequest request, final int line) {
        assertThat(request.getCommit(line)).isEqualTo(COMMIT);
        assertThat(request.getName(line)).isEqualTo(NAME);
        assertThat(request.getEmail(line)).isEqualTo(EMAIL);
    }

    private void setDetails(final BlameRequest request, final int lineNumber) {
        request.setCommit(lineNumber, COMMIT);
        request.setName(lineNumber, NAME);
        request.setEmail(lineNumber, EMAIL);
    }

    @Test
    void shouldMergeRequest() {
        BlameRequest request = new BlameRequest("file", 1);
        setDetails(request, 1);

        BlameRequest sameLine = new BlameRequest("file", 1);
        request.merge(sameLine);
        assertThat(request).containsExactly(1);
        verifyDetails(request, 1);

        BlameRequest otherLine = new BlameRequest("file", 2);
        setDetails(otherLine, 2);

        request.merge(otherLine);
        assertThat(request.iterator()).toIterable().containsExactly(1, 2);
        verifyDetails(request, 1);
        verifyDetails(request, 2);

        assertThatThrownBy(() -> request.merge(new BlameRequest("wrong", 3)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wrong").hasMessageContaining("file");
    }

    @Test
    void shouldReturnMeaningfulDefaults() {
        BlameRequest request = new BlameRequest("file", 1);

        assertThat(request.getCommit(2)).isEqualTo(EMPTY);
        assertThat(request.getEmail(2)).isEqualTo(EMPTY);
        assertThat(request.getName(2)).isEqualTo(EMPTY);
    }

}