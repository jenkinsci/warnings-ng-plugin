package io.jenkins.plugins.analysis.warnings;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.SourceRoot;
import io.jenkins.plugins.analysis.core.util.GlobalConfigurationFacade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link WarningsPluginConfiguration}.
 *
 * @author Ullrich Hafner
 */
class WarningsPluginConfigurationTest {
    private static final List<SourceRoot> SOURCE_ROOTS = Arrays.asList(new SourceRoot("One"), new SourceRoot("Two"));

    @Test
    void shouldHaveNoRootFoldersWhenCreated() {
        WarningsPluginConfiguration configuration = new WarningsPluginConfiguration(mock(GlobalConfigurationFacade.class));

        assertThat(configuration.getSourceRoots()).isEmpty();
        assertThat(configuration.getSourceRootFolders()).isEmpty();
    }

    @Test
    void shouldSaveConfigurationIfFoldersAreAdded() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);

        WarningsPluginConfiguration configuration = new WarningsPluginConfiguration(facade);
        configuration.setSourceRoots(SOURCE_ROOTS);

        verify(facade).save();
        assertThat(configuration.getSourceRoots()).isEqualTo(SOURCE_ROOTS);
        assertThat(configuration.getSourceRootFolders()).contains("One", "Two");
    }
}
