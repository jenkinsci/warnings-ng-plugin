package io.jenkins.plugins.analysis.core.scm;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.assertThat;

/**
 * Tests the class {@link Blames}.
 *
 * @author Ullrich Hafner
 */
class BlamesTest {
    private static final String RELATIVE_PATH = "with/file.txt";
    private static final String WORKSPACE = "/absolute/path/to/workspace/";
    private static final String ABSOLUTE_PATH = WORKSPACE + RELATIVE_PATH;

    @Test
    void shouldCreateEmptyDefaultValue() {
        Blames empty = new Blames();
        
        assertThat(empty).isEmpty();
        assertThat(empty.size()).isEqualTo(0);
    }
    
    @Test
    void shouldCreateSingleBlame() {
        Blames blames = new Blames(WORKSPACE);

        blames.addLine(ABSOLUTE_PATH, 1);

        assertThat(blames).isNotEmpty();
        assertThat(blames.size()).isEqualTo(1);
        assertThat(blames).hasFiles(ABSOLUTE_PATH);
        assertThat(blames.contains(ABSOLUTE_PATH)).isTrue();
        assertThat(blames.getRequest(ABSOLUTE_PATH)).hasFileName(RELATIVE_PATH);
        assertThat(blames.getRequest(ABSOLUTE_PATH).iterator()).containsExactlyInAnyOrder(1);
    }
    
    @Test
    void shouldAddAdditionalLinesToARequest() {
        Blames blames = new Blames(WORKSPACE);

        blames.addLine(ABSOLUTE_PATH, 1);
        blames.addLine(ABSOLUTE_PATH, 2);

        assertThat(blames.size()).isEqualTo(1);
        assertThat(blames).hasFiles(ABSOLUTE_PATH);
        assertThat(blames.getRequest(ABSOLUTE_PATH)).hasFileName(RELATIVE_PATH);
        assertThat(blames.getRequest(ABSOLUTE_PATH).iterator()).containsExactlyInAnyOrder(1, 2);
    }
}