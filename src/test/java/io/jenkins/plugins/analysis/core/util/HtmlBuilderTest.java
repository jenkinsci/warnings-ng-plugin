package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link HtmlBuilder}.
 *
 * @author Ullrich Hafner
 */
class HtmlBuilderTest {
    @Test
    void shouldCreateLinks() {
        assertThat(new HtmlBuilder().link("http://link.de", "Text").build())
                .isEqualTo("<a href=\"http://link.de\">Text</a>");
        assertThat(new HtmlBuilder().linkWithClass("http://link.de", "Text", "a b c").build())
                .isEqualTo("<a href=\"http://link.de\" class=\"a b c\">Text</a>");
    }
}