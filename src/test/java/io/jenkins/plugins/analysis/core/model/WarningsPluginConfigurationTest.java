package io.jenkins.plugins.analysis.core.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.PathUtil;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import io.jenkins.plugins.analysis.core.util.GlobalConfigurationFacade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link WarningsPluginConfiguration}.
 *
 * @author Ullrich Hafner
 */
class WarningsPluginConfigurationTest {
    private static final String FIRST = "/One";
    private static final String SECOND = "/Two";
    private static final String ABSOLUTE = "/Three";
    private static final String RELATIVE = "Relative";
    private static final String WORKSPACE_PATH = "/workspace";
    private static final FilePath WORKSPACE = new FilePath((VirtualChannel) null, WORKSPACE_PATH);

    private static final List<SourceDirectory> SOURCE_ROOTS
            = asList(new SourceDirectory(FIRST), new SourceDirectory(SECOND));
    private static final PathUtil PATH_UTIL = new PathUtil();

    @Test
    void shouldHaveNoRootFoldersWhenCreated() {
        WarningsPluginConfiguration configuration = createConfiguration();

        assertThat(configuration.getSourceDirectories()).isEmpty();
        assertThat(configuration.getPermittedSourceDirectories(WORKSPACE, Collections.emptyList()))
                .containsExactly(WORKSPACE_PATH);
        assertThat(configuration.getPermittedSourceDirectories(WORKSPACE, asList(ABSOLUTE)))
                .containsExactly(WORKSPACE_PATH);
        assertThat(configuration.getPermittedSourceDirectories(WORKSPACE, asList(RELATIVE)))
                .containsExactly(WORKSPACE_PATH, getWorkspacePath(RELATIVE));
    }

    @Test
    void shouldSaveConfigurationIfFoldersAreAdded() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);

        WarningsPluginConfiguration configuration = new WarningsPluginConfiguration(facade);
        configuration.setSourceDirectories(SOURCE_ROOTS);

        verify(facade).save();
        assertThat(configuration.getSourceDirectories()).isEqualTo(SOURCE_ROOTS);
        assertThat(configuration.getPermittedSourceDirectories(WORKSPACE, asList(FIRST)))
                .containsExactly(WORKSPACE_PATH, FIRST);
        assertThat(configuration.getPermittedSourceDirectories(WORKSPACE, asList(FIRST, SECOND)))
                .containsExactly(WORKSPACE_PATH, FIRST, SECOND);
        assertThat(configuration.getPermittedSourceDirectories(WORKSPACE, asList(FIRST, SECOND, ABSOLUTE)))
                .containsExactly(WORKSPACE_PATH, FIRST, SECOND);
        assertThat(configuration.getPermittedSourceDirectories(WORKSPACE, asList(FIRST, SECOND, RELATIVE)))
                .containsExactly(WORKSPACE_PATH, FIRST, SECOND, getWorkspacePath(RELATIVE));
    }

    @Test
    void shouldNormalizePath() {
        WarningsPluginConfiguration configuration = createConfiguration();

        configuration.setSourceDirectories(asList(
                new SourceDirectory("/absolute/unix"),
                new SourceDirectory("C:\\absolute\\windows")));

        String relativeUnix = "relative/unix";
        String relativeWindows = "relative\\windows";
        String absoluteUnix = "/absolute/unix";
        String absoluteWindows = "C:\\absolute\\windows";
        String absoluteWindowsNormalized = "C:/absolute/windows";
        assertThat(configuration.getPermittedSourceDirectories(WORKSPACE,
                asList(relativeUnix, relativeWindows, absoluteUnix, absoluteWindows, absoluteWindowsNormalized)))
                .containsExactly(WORKSPACE_PATH, getWorkspacePath(relativeUnix), getWorkspacePath(relativeWindows),
                        absoluteUnix, PATH_UTIL.getAbsolutePath(absoluteWindows), absoluteWindowsNormalized);
    }

    private String getWorkspacePath(final String relative) {
        return PATH_UTIL.getAbsolutePath(WORKSPACE_PATH + "/" + relative);
    }

    @SafeVarargs
    private static <T> List<T> asList(final T... paths) {
        return Arrays.asList(paths);
    }

    private WarningsPluginConfiguration createConfiguration() {
        return new WarningsPluginConfiguration(mock(GlobalConfigurationFacade.class));
    }
}
