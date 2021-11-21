package io.jenkins.plugins.analysis.core.model;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import hudson.model.Run;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link InfoErrorDetail}.
 *
 * @author Ullrich Hafner
 */
class InfoErrorDetailTest {
    private static final String TITLE = "Title";
    private static final String DISPLAY_NAME = TITLE + " - Messages";
    private static final ImmutableList<String> INFO_MESSAGES = Lists.immutable.of("One", "Two");
    private static final ImmutableList<String> ERROR_MESSAGES = Lists.immutable.of("Error One", "Error Two");

    @Test
    void shouldCreateMessagesAndErrors() {
        Run<?, ?> build = mock(Run.class);
        InfoErrorDetail model = new InfoErrorDetail(build, ERROR_MESSAGES, INFO_MESSAGES, TITLE);

        assertThat(model.getDisplayName()).isEqualTo(DISPLAY_NAME);
        assertThat(model.getOwner()).isSameAs(build);
        assertThat(model.getInfoMessages()).containsExactlyElementsOf(INFO_MESSAGES);
        assertThat(model.getErrorMessages()).containsExactlyElementsOf(ERROR_MESSAGES);
    }
}
