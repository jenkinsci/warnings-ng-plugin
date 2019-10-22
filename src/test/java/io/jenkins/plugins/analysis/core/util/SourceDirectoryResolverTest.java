package io.jenkins.plugins.analysis.core.util;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link SourceDirectoryResolver}.
 *
 * @author Ullrich Hafner
 */
class SourceDirectoryResolverTest {
    private static final String WORKSPACE = "/workspace";
    private static final String ABSOLUTE = "/absolute";
    private static final String RELATIVE = "relative";
    private static final String RESOLVED = WORKSPACE + "/" + RELATIVE;

    @Test
    void shouldCreateSingletonOrEmptyCollection() {
        SourceDirectoryResolver handler = new SourceDirectoryResolver();

        assertThat(handler.asCollection("")).isEmpty();
        assertThat(handler.asCollection("relative")).containsExactly("relative");
        assertThat(handler.asCollection("/absolute")).containsExactly("/absolute");
    }

    @Test
    void shouldExpandRelativePaths() {
        SourceDirectoryResolver handler = new SourceDirectoryResolver();

        assertThat(handler.toAbsolutePaths(WORKSPACE, Collections.emptyList()))
                .containsExactly(WORKSPACE);
        assertThat(handler.toAbsolutePaths(WORKSPACE, Collections.singleton(WORKSPACE)))
                .containsExactly(WORKSPACE);
        assertThat(handler.toAbsolutePaths(WORKSPACE, Collections.singleton(ABSOLUTE)))
                .containsExactly(WORKSPACE, ABSOLUTE);
        assertThat(handler.toAbsolutePaths(WORKSPACE, Arrays.asList(ABSOLUTE, RELATIVE)))
                .containsExactly(WORKSPACE, ABSOLUTE, RESOLVED);
        assertThat(handler.toAbsolutePaths(WORKSPACE, Arrays.asList(ABSOLUTE, RELATIVE, RELATIVE)))
                .containsExactly(WORKSPACE, ABSOLUTE, RESOLVED);
        assertThat(handler.toAbsolutePaths(WORKSPACE, Arrays.asList(RELATIVE, ABSOLUTE)))
                .containsExactly(WORKSPACE, RESOLVED, ABSOLUTE);
        assertThat(handler.toAbsolutePaths(WORKSPACE, Arrays.asList(RELATIVE, ABSOLUTE, ABSOLUTE)))
                .containsExactly(WORKSPACE, RESOLVED, ABSOLUTE);
    }
}
