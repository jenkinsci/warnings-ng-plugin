package io.jenkins.plugins.analysis.core.model;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.PathUtil;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import io.jenkins.plugins.util.GlobalConfigurationFacade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link WarningsPluginConfiguration}.
 *
 * @author Ullrich Hafner
 */
class WarningsPluginConfigurationTest {
    private static final PathUtil PATH_UTIL = new PathUtil();

    private static final String FIRST = "/One";
    private static final String SECOND = "/Two";
    private static final String ABSOLUTE_NOT_EXISTING = "/Three";
    private static final String RELATIVE = "Relative";

    private static final String NORMALIZED = PATH_UTIL.getAbsolutePath("/workspace");

    private static final List<SourceDirectory> SOURCE_ROOTS
            = Arrays.asList(new SourceDirectory(FIRST), new SourceDirectory(SECOND));

    @Test
    void shouldHaveNoRootFoldersWhenCreated() {
        WarningsPluginConfiguration configuration = createConfiguration();

        assertThat(configuration.getSourceDirectories()).isEmpty();

        assertThat(get(configuration, "")).isEqualTo(NORMALIZED);
        assertThat(get(configuration, "-")).isEqualTo(NORMALIZED);
        assertThat(get(configuration, ABSOLUTE_NOT_EXISTING)).isEqualTo(NORMALIZED);
        assertThat(get(configuration, RELATIVE)).isEqualTo(getWorkspaceChild(RELATIVE));
    }

    @Test
    void shouldSaveConfigurationIfFoldersAreAdded() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);
        WarningsPluginConfiguration configuration = new WarningsPluginConfiguration(facade);

        configuration.setSourceDirectories(SOURCE_ROOTS);

        verify(facade).save();
        assertThat(configuration.getSourceDirectories()).isEqualTo(SOURCE_ROOTS);

        assertThat(get(configuration, FIRST)).isEqualTo(FIRST);
        assertThat(get(configuration, RELATIVE)).isEqualTo(getWorkspaceChild(RELATIVE));
        assertThat(get(configuration, ABSOLUTE_NOT_EXISTING)).isEqualTo(NORMALIZED);
    }

    @Test
    void shouldNormalizePath() {
        WarningsPluginConfiguration configuration = createConfiguration();

        configuration.setSourceDirectories(
                Arrays.asList(new SourceDirectory("/absolute/unix"), new SourceDirectory("C:\\absolute\\windows")));

        String relativeUnix = "relative/unix";
        String relativeWindows = "relative\\windows";
        String absoluteUnix = "/absolute/unix";
        String absoluteWindows = "C:\\absolute\\windows";
        String absoluteWindowsNormalized = "C:/absolute/windows";

        assertThat(get(configuration, relativeUnix)).isEqualTo(getWorkspaceChild(relativeUnix));
        assertThat(get(configuration, relativeWindows)).isEqualTo(getWorkspaceChild(relativeWindows));
        assertThat(get(configuration, absoluteUnix)).isEqualTo(absoluteUnix);
        assertThat(get(configuration, absoluteWindows)).isEqualTo(normalize(absoluteWindows));
        assertThat(get(configuration, absoluteWindowsNormalized)).isEqualTo(absoluteWindowsNormalized);
    }

    private String getWorkspaceChild(final String expected) {
        return PATH_UTIL.createAbsolutePath(NORMALIZED, expected);
    }

    private String normalize(final String remote) {
        return PATH_UTIL.getAbsolutePath(remote);
    }

    private String get(final WarningsPluginConfiguration configuration, final String absoluteUnix) {
        FilePath path = new FilePath((VirtualChannel) null, NORMALIZED);
        return normalize(configuration.getPermittedSourceDirectory(path, absoluteUnix).getRemote());
    }

    private WarningsPluginConfiguration createConfiguration() {
        return new WarningsPluginConfiguration(mock(GlobalConfigurationFacade.class));
    }
}
