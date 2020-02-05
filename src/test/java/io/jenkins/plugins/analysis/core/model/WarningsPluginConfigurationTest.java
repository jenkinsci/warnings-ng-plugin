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
    private static final String FIRST = "/One";
    private static final String SECOND = "/Two";
    private static final String ABSOLUTE = "/Three";
    private static final String RELATIVE = "Relative";
    private static final String WORKSPACE_PATH = "/workspace";
    private static final FilePath WORKSPACE_FILE_PATH = createPath(WORKSPACE_PATH);
    private static final FilePath WORKSPACE = WORKSPACE_FILE_PATH;

    private static FilePath createPath(final String path) {
        return new FilePath((VirtualChannel) null, path);
    }

    private static final List<SourceDirectory> SOURCE_ROOTS
            = asList(new SourceDirectory(FIRST), new SourceDirectory(SECOND));
    private static final PathUtil PATH_UTIL = new PathUtil();

    @Test
    void shouldHaveNoRootFoldersWhenCreated() {
        WarningsPluginConfiguration configuration = createConfiguration();

        assertThat(configuration.getSourceDirectories()).isEmpty();
        assertThat(configuration.getPermittedSourceDirectory(WORKSPACE, ""))
                .isEqualTo(WORKSPACE_FILE_PATH);
        assertThat(configuration.getPermittedSourceDirectory(WORKSPACE, "-"))
                .isEqualTo(WORKSPACE_FILE_PATH);
        assertThat(configuration.getPermittedSourceDirectory(WORKSPACE, ABSOLUTE))
                .isEqualTo(WORKSPACE_FILE_PATH);
        assertThat(configuration.getPermittedSourceDirectory(WORKSPACE, RELATIVE))
                .isEqualTo(getWorkspacePath(RELATIVE));
    }

    @Test
    void shouldSaveConfigurationIfFoldersAreAdded() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);

        WarningsPluginConfiguration configuration = new WarningsPluginConfiguration(facade);
        configuration.setSourceDirectories(SOURCE_ROOTS);

        verify(facade).save();
        assertThat(configuration.getSourceDirectories()).isEqualTo(SOURCE_ROOTS);
        assertThat(configuration.getPermittedSourceDirectory(WORKSPACE, FIRST))
                .isEqualTo(createPath(FIRST));
        assertThat(configuration.getPermittedSourceDirectory(WORKSPACE, ABSOLUTE))
                .isEqualTo(WORKSPACE_FILE_PATH);
        assertThat(configuration.getPermittedSourceDirectory(WORKSPACE, RELATIVE))
                .isEqualTo(getWorkspacePath(RELATIVE));
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
        assertThat(configuration.getPermittedSourceDirectory(WORKSPACE,
                relativeUnix))
                .isEqualTo(getWorkspacePath(relativeUnix));
        assertThat(configuration.getPermittedSourceDirectory(WORKSPACE,
                relativeWindows))
                .isEqualTo(getWorkspacePath(relativeWindows));
        assertThat(configuration.getPermittedSourceDirectory(WORKSPACE,
                absoluteUnix))
                .isEqualTo(createPath(absoluteUnix));
        assertThat(configuration.getPermittedSourceDirectory(WORKSPACE,
                absoluteWindows))
                .isEqualTo(createPath(PATH_UTIL.getAbsolutePath(absoluteWindows)));
        assertThat(configuration.getPermittedSourceDirectory(WORKSPACE,
                absoluteWindowsNormalized))
                .isEqualTo(createPath(absoluteWindowsNormalized));
    }

    private FilePath getWorkspacePath(final String relative) {
        return createPath(PATH_UTIL.getAbsolutePath(WORKSPACE_PATH + "/" + relative));
    }

    @SafeVarargs
    private static <T> List<T> asList(final T... paths) {
        return Arrays.asList(paths);
    }

    private WarningsPluginConfiguration createConfiguration() {
        return new WarningsPluginConfiguration(mock(GlobalConfigurationFacade.class));
    }
}
