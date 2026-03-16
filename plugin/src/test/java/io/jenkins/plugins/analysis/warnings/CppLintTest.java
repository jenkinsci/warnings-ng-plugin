package io.jenkins.plugins.analysis.warnings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link CppLint} class.
 *
 * @author ClawOSS
 */
class CppLintTest {
    @Test
    void shouldReturnCorrectDisplayName() {
        CppLint.Descriptor descriptor = new CppLint.Descriptor();
        var labelProvider = descriptor.getLabelProvider();
        
        assertThat(labelProvider.getName()).isEqualTo("C++ Lint");
        assertThat(labelProvider.getName()).doesNotContain("&#43;");
    }
}

