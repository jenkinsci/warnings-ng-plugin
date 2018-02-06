package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

/**
 * Tests the class {@link AffectedFilesResolver}.
 *
 * @author Ullrich Hafner
 */
class AffectedFilesResolverTest {
    private static final FilePath BUILD_ROOT = new FilePath(new File("builds"));

    /** Ensures that illegal file names are processed without problems. */
    @ParameterizedTest(name = "[{index}] Illegal filename = {0}")
    @ValueSource(strings = {"/does/not/exist", "!<>$$&%/&(", "\0 Null-Byte"})
    void shouldReturnFallbackOnError(final String fileName) throws IOException, InterruptedException {
        String message = new AffectedFilesResolver().copyFilesWithAnnotationsToBuildFolder(
                mock(VirtualChannel.class), BUILD_ROOT, "UTF-8", Collections.singleton(fileName));
        assertThat(message).isEqualTo("0 copied, 1 not-found, 0 with I/O error");
    }
}