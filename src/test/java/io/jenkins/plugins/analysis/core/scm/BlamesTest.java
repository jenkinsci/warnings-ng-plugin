package io.jenkins.plugins.analysis.core.scm;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.FilteredLog;
import static io.jenkins.plugins.analysis.core.assertions.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Tests the class {@link Blames}.
 *
 * @author Ullrich Hafner
 */
class BlamesTest {
    private static final String RELATIVE_PATH = "with/file.txt";
    private static final String WORKSPACE = "/absolute/path/to/workspace/";
    private static final String ABSOLUTE_PATH = WORKSPACE + RELATIVE_PATH;
    private static final String WINDOWS_WORKSPACE = "C:\\absolute\\path\\to\\workspace\\";
    private static final String WINDOWS_RELATIVE_PATH = "with/file.txt";
    private static final String WINDOWS_ABSOLUTE_PATH = "C:/absolute/path/to/workspace/" + WINDOWS_RELATIVE_PATH;
    private static final String ANOTHER_FILE = "another-file.txt";
    private static final String ANOTHER_ABSOLUTE_PATH = WORKSPACE + ANOTHER_FILE;
    private static final String COMMIT = "commit";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final FilteredLog LOG = mock(FilteredLog.class);

    @Test
    void shouldCreateEmptyDefaultValue() {
        Blames empty = new Blames();
        
        assertThat(empty).isEmpty();
        assertThat(empty.size()).isEqualTo(0);
    }
    
    @Test
    void shouldCreateSingleBlame() {
        Blames blames = new Blames(WORKSPACE);

        blames.addLine(ABSOLUTE_PATH, 1, LOG);

        assertThat(blames).isNotEmpty();
        assertThat(blames.size()).isEqualTo(1);
        assertThat(blames).hasFiles(ABSOLUTE_PATH);
        assertThat(blames.contains(ABSOLUTE_PATH)).isTrue();
        
        assertThat(blames.getRequests()).containsExactly(new BlameRequest(RELATIVE_PATH, 1));
    }
    
    @Test
    void shouldConvertWindowsPathToUnix() {
        Blames blames = new Blames(WINDOWS_WORKSPACE);

        blames.addLine(WINDOWS_ABSOLUTE_PATH, 1, LOG);

        assertThat(blames).isNotEmpty();
        assertThat(blames.size()).isEqualTo(1);
        assertThat(blames).hasFiles(WINDOWS_ABSOLUTE_PATH);
        assertThat(blames.contains(WINDOWS_ABSOLUTE_PATH)).isTrue();
        
        assertThat(blames.getRequests()).containsExactly(new BlameRequest(RELATIVE_PATH, 1));
    }
    
    @Test
    void shouldAddAdditionalLinesToRequest() {
        Blames blames = new Blames(WORKSPACE);

        blames.addLine(ABSOLUTE_PATH, 1, LOG);
        blames.addLine(ABSOLUTE_PATH, 2, LOG);

        assertThat(blames.size()).isEqualTo(1);
        assertThat(blames).hasFiles(ABSOLUTE_PATH);

        BlameRequest expectedBlame = new BlameRequest(RELATIVE_PATH, 1).addLineNumber(2);
        assertThat(blames.getRequests()).containsExactly(expectedBlame);
        assertThat(blames.get(ABSOLUTE_PATH)).isEqualTo(expectedBlame);
    }

    @Test
    void shouldCreateTwoDifferentBlames() {
        Blames blames = new Blames(WORKSPACE);

        blames.addLine(ABSOLUTE_PATH, 1, LOG);
        blames.addLine(WORKSPACE + ANOTHER_FILE, 2, LOG);

        assertThat(blames.size()).isEqualTo(2);
        assertThat(blames).hasFiles(ABSOLUTE_PATH, ANOTHER_ABSOLUTE_PATH);

        BlameRequest firstBlame = new BlameRequest(RELATIVE_PATH, 1);
        BlameRequest secondBlame = new BlameRequest(ANOTHER_FILE, 2);
        
        assertThat(blames.getRequests()).containsExactlyInAnyOrder(firstBlame, secondBlame);
        assertThat(blames.get(ABSOLUTE_PATH)).isEqualTo(firstBlame);
        assertThat(blames.get(ANOTHER_ABSOLUTE_PATH)).isEqualTo(secondBlame);
        
        String wrongFile = "wrong file";
        assertThatThrownBy(() -> blames.get(wrongFile))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(wrongFile);
    }
    
    @Test
    void shouldAggregateBlamesOfSameFile() {
        Blames blames = new Blames(WORKSPACE);

        blames.addLine(ABSOLUTE_PATH, 1, LOG);
        setDetails(blames.get(ABSOLUTE_PATH), 1);
        blames.addLine(ABSOLUTE_PATH, 2, LOG);
        setDetails(blames.get(ABSOLUTE_PATH), 2);

        Blames another = new Blames(WORKSPACE);

        another.addLine(ABSOLUTE_PATH, 2, LOG);
        setDetails(another.get(ABSOLUTE_PATH), 2);
        another.addLine(ABSOLUTE_PATH, 3, LOG);
        setDetails(another.get(ABSOLUTE_PATH), 3);
        
        blames.addAll(another);

        BlameRequest expected = new BlameRequest(RELATIVE_PATH, 1).addLineNumber(2).addLineNumber(3);
        setDetails(expected, 1);
        setDetails(expected, 2);
        setDetails(expected, 3);
        assertThat(blames.getRequests()).containsExactly(expected);
    }

    private void setDetails(final BlameRequest request, final int lineNumber) {
        request.setCommit(lineNumber, COMMIT);
        request.setName(lineNumber, NAME);
        request.setEmail(lineNumber, EMAIL);
    }


    @Test
    void shouldAggregateBlamesOfDifferentFiles() {
        Blames blames = new Blames(WORKSPACE);

        blames.addLine(ABSOLUTE_PATH, 1, LOG);
        blames.addLine(ABSOLUTE_PATH, 2, LOG);

        Blames another = new Blames(WORKSPACE);

        another.addLine(ANOTHER_ABSOLUTE_PATH, 2, LOG);
        another.addLine(ANOTHER_ABSOLUTE_PATH, 3, LOG);
        
        blames.addAll(another);

        BlameRequest firstBlame = new BlameRequest(RELATIVE_PATH, 1).addLineNumber(2);
        BlameRequest secondBlame = new BlameRequest(ANOTHER_FILE, 2).addLineNumber(3);

        assertThat(blames.getRequests()).containsExactlyInAnyOrder(firstBlame, secondBlame);
        assertThat(blames.get(ABSOLUTE_PATH)).isEqualTo(firstBlame);
        assertThat(blames.get(ANOTHER_ABSOLUTE_PATH)).isEqualTo(secondBlame);
    }
}