package io.jenkins.plugins.analysis.warnings;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.warnings.PyLint.PyLintDescriptions;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class {@link PyLintDescriptions}.
 *
 * @author Ullrich Hafner
 */
class PyLintTest {
    @Test
    void shouldReturnCorrectDescription() {
        PyLintDescriptions descriptions = new PyLintDescriptions();

        assertThat(descriptions.initialize()).isEqualTo(274);
        assertThat(descriptions.getDescription("singleton-comparison"))
                .isEqualTo("Used when an expression is compared to singleton values like True, False orNone.");
        assertThat(descriptions.getDescription("C0326"))
                .isEqualTo("Used when a wrong number of spaces is used around an operator, bracket orblock opener.");
        assertThat(descriptions.getDescription("something-wrong"))
                .isEqualTo(PyLintDescriptions.NO_DESCRIPTION_FOUND);
    }
}