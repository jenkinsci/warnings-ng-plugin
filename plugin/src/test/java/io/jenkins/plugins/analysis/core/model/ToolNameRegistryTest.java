package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import hudson.model.Run;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ToolNameRegistry}.
 *
 * @author Akash Manna
 */
class ToolNameRegistryTest {
    @Test
    void shouldCreateEmptyRegistry() {
        ToolNameRegistry registry = new ToolNameRegistry();

        assertThat(registry.size()).isEqualTo(0);
        assertThat(registry.contains("checkstyle")).isFalse();
    }

    @Test
    void shouldRegisterAndRetrieveNames() {
        ToolNameRegistry registry = new ToolNameRegistry();

        registry.register("checkstyle", "CheckStyle Warnings");
        registry.register("spotbugs", "SpotBugs");

        assertThat(registry.size()).isEqualTo(2);
        assertThat(registry.contains("checkstyle")).isTrue();
        assertThat(registry.contains("spotbugs")).isTrue();
        assertThat(registry.getName("checkstyle")).isEqualTo("CheckStyle Warnings");
        assertThat(registry.getName("spotbugs")).isEqualTo("SpotBugs");
    }

    @Test
    void shouldNotEscapeHtmlInNames() {
        ToolNameRegistry registry = new ToolNameRegistry();

        registry.register("custom", "<script>alert('xss')</script>");

        // Names are no longer HTML-escaped here; escaping should happen at the presentation layer
        assertThat(registry.getName("custom")).isEqualTo("<script>alert('xss')</script>");
        assertThat(registry.asMap().get("custom")).isEqualTo("<script>alert('xss')</script>");
    }

    @Test
    void shouldReturnIdForUnknownId() {
        ToolNameRegistry registry = new ToolNameRegistry();
        
        registry.register("unknown", "unknown");
        assertThat(registry.getName("unknown")).isEqualTo("unknown");
        
        // Names are no longer HTML-escaped; the raw name is returned
        registry.register("<script>", "<script>");
        assertThat(registry.getName("<script>")).isEqualTo("<script>");
    }

    @Test
    void shouldCreateRegistryFromBuild() {
        Run<?, ?> build = mock(Run.class);

        ResultAction checkstyleAction = mock(ResultAction.class);
        when(checkstyleAction.getId()).thenReturn("checkstyle");
        when(checkstyleAction.getName()).thenReturn("CheckStyle");

        ResultAction spotbugsAction = mock(ResultAction.class);
        when(spotbugsAction.getId()).thenReturn("spotbugs");
        when(spotbugsAction.getName()).thenReturn("SpotBugs");

        when(build.getActions(ResultAction.class)).thenReturn(java.util.List.of(checkstyleAction, spotbugsAction));

        ToolNameRegistry registry = ToolNameRegistry.fromBuild(build);

        assertThat(registry.size()).isEqualTo(2);
        assertThat(registry.getName("checkstyle")).isEqualTo("CheckStyle");
        assertThat(registry.getName("spotbugs")).isEqualTo("SpotBugs");
    }

    @Test
    void shouldFallbackToIdForRegisteredIds() {
        ToolNameRegistry registry = new ToolNameRegistry();
        
        registry.register("checkstyle", "CheckStyle");
        registry.register("unknownToolId", "Unknown Tool");

        assertThat(registry.getName("checkstyle")).isEqualTo("CheckStyle");
        assertThat(registry.getName("unknownToolId")).isEqualTo("Unknown Tool");
    }
}
