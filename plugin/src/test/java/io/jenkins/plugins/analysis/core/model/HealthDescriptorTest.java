package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.util.HealthDescriptor;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the class {@link HealthDescriptor}.
 *
 * @author Ullrich Hafner
 */
class HealthDescriptorTest {
    @Test
    void shouldValidateEnabled() {
        assertThat(new HealthDescriptor(0, 0, Severity.WARNING_NORMAL))
                .isNotEnabled().isNotValid();
        assertThat(new HealthDescriptor(0, 1, Severity.WARNING_NORMAL))
                .isEnabled().isNotValid();
        assertThat(new HealthDescriptor(1, 0, Severity.WARNING_NORMAL))
                .isEnabled().isNotValid();
        assertThat(new HealthDescriptor(1, 1, Severity.WARNING_NORMAL))
                .isEnabled().isNotValid();
        assertThat(new HealthDescriptor(1, 2, Severity.WARNING_NORMAL))
                .isEnabled().isValid();
    }
}