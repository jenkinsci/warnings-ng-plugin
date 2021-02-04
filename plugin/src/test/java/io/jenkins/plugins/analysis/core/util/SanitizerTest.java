package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link Sanitizer}.
 *
 * @author Ullrich Hafner
 */
class SanitizerTest {
    @Test @Issue("JENKINS-61834")
    void shouldSkipPlus() {
        Sanitizer sanitizer = new Sanitizer();

        assertThat(sanitizer.render("C++")).isEqualTo("C&#43;&#43;");
    }
}
