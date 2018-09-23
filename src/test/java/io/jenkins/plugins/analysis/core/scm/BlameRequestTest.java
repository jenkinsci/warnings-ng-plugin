package io.jenkins.plugins.analysis.core.scm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    void should() {
        BlameRequest request = new BlameRequest("file", 15);
        
        assertThat(request).hasSize(1).containsExactly(15);
        assertThat(request.getFileName()).isEqualTo("file");
        
        request.addLineNumber(25);
        assertThat(request).hasSize(2).containsExactlyInAnyOrder(15, 25);
        
        request.setCommit(25, COMMIT);
        request.setName(25, NAME);
        request.setEmail(25, EMAIL);
        
        assertThat(request.getCommit(25)).isEqualTo(COMMIT);
        assertThat(request.getName(25)).isEqualTo(NAME);
        assertThat(request.getEmail(25)).isEqualTo(EMAIL);
    }
}