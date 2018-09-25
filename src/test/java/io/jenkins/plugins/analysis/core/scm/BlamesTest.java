package io.jenkins.plugins.analysis.core.scm;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the class {@link Blames}.
 *
 * @author Ullrich Hafner
 */
class BlamesTest {
    private static final String RELATIVE_PATH = "with/file.txt";
    private static final String WORKSPACE = "/absolute/path/to/workspace/";
    private static final String ABSOLUTE_PATH = WORKSPACE + RELATIVE_PATH;
    private static final String ANOTHER_FILE = "another-file.txt";
    private static final String ANOTHER_ABSOLUTE_PATH = WORKSPACE + ANOTHER_FILE;

    @Test
    void shouldCreateEmptyDefaultValue() {
        Blames empty = new Blames();
        
        assertThat(empty).isEmpty();
        assertThat(empty.size()).isEqualTo(0);
    }
    
    @Test
    void shouldHaveErrorAndInfoMessages() {
        Blames blames = new Blames();
        
        blames.logError("error %d", 1);
        blames.logInfo("message %d", 2);
        
        assertThat(blames).hasErrorMessages("error 1");
        assertThat(blames).hasInfoMessages("message 2");
    }
    
    @Test
    void shouldCreateSingleBlame() {
        Blames blames = new Blames(WORKSPACE);

        blames.addLine(ABSOLUTE_PATH, 1);

        assertThat(blames).isNotEmpty();
        assertThat(blames.size()).isEqualTo(1);
        assertThat(blames).hasFiles(ABSOLUTE_PATH);
        assertThat(blames.contains(ABSOLUTE_PATH)).isTrue();
        
        assertThat(blames.getRequests()).containsExactly(new BlameRequest(RELATIVE_PATH, 1));
    }
    
    @Test
    void shouldAddAdditionalLinesToRequest() {
        Blames blames = new Blames(WORKSPACE);

        blames.addLine(ABSOLUTE_PATH, 1);
        blames.addLine(ABSOLUTE_PATH, 2);

        assertThat(blames.size()).isEqualTo(1);
        assertThat(blames).hasFiles(ABSOLUTE_PATH);

        BlameRequest expectedBlame = new BlameRequest(RELATIVE_PATH, 1).addLineNumber(2);
        assertThat(blames.getRequests()).containsExactly(expectedBlame);
        assertThat(blames.get(ABSOLUTE_PATH)).isEqualTo(expectedBlame);
    }

    @Test
    void shouldCreateTwoDifferentBlames() {
        Blames blames = new Blames(WORKSPACE);

        blames.addLine(ABSOLUTE_PATH, 1);
        blames.addLine(WORKSPACE + ANOTHER_FILE, 2);

        assertThat(blames.size()).isEqualTo(2);
        assertThat(blames).hasFiles(ABSOLUTE_PATH, ANOTHER_ABSOLUTE_PATH);

        BlameRequest firstBlame = new BlameRequest(RELATIVE_PATH, 1);
        BlameRequest secondBlame = new BlameRequest(ANOTHER_FILE, 2);
        
        assertThat(blames.getRequests()).containsExactlyInAnyOrder(firstBlame, secondBlame);
        assertThat(blames.get(ABSOLUTE_PATH)).isEqualTo(firstBlame);
        assertThat(blames.get(ANOTHER_ABSOLUTE_PATH)).isEqualTo(secondBlame);
        
        String wrongFile = "wrong file";
        assertThatThrownBy(() -> { blames.get(wrongFile); })
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(wrongFile);
    }
}