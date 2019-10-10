package io.jenkins.plugins.analysis.core.model;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.util.GlobalConfigurationFacade;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link WarningsPluginConfiguration}.
 *
 * @author Ullrich Hafner
 */
class WarningsPluginConfigurationTest {
    private static final List<SourceDirectory> SOURCE_ROOTS = asList(new SourceDirectory("One"), new SourceDirectory("Two"));

    @Test
    void shouldHaveNoRootFoldersWhenCreated() {
        WarningsPluginConfiguration configuration = new WarningsPluginConfiguration(mock(GlobalConfigurationFacade.class));

        assertThat(configuration.getSourceDirectories()).isEmpty();
        assertThat(configuration.getPermittedSourceDirectories(Collections.emptyList())).isEmpty();
    }

    @Test
    void shouldSaveConfigurationIfFoldersAreAdded() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);

        WarningsPluginConfiguration configuration = new WarningsPluginConfiguration(facade);
        configuration.setSourceDirectories(SOURCE_ROOTS);

        verify(facade).save();
        assertThat(configuration.getSourceDirectories()).isEqualTo(SOURCE_ROOTS);
        assertThat(configuration.getPermittedSourceDirectories(Collections.singletonList("One"))).containsExactly("One");
        assertThat(configuration.getPermittedSourceDirectories(asList("One", "Two"))).containsExactly("One", "Two");
        assertThat(configuration.getPermittedSourceDirectories(asList("One", "Two", "Three"))).containsExactly("One", "Two");
    }
}
