package io.jenkins.plugins.analysis.core.quality;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.quality.StatusAssert.*;

import hudson.model.Result;

/**
 * Tests the class {@link Status}.
 *
 * @author Ullrich Hafner
 */
class StatusTest {
    @Test
    void shouldIdentifySuccessfulStatus() {
        assertThat(Status.PASSED)
                .isSuccessful()
                .hasColor(Result.SUCCESS.color);
        assertThat(Status.INACTIVE)
                .isSuccessful()
                .hasColor(Result.NOT_BUILT.color);
        assertThat(Status.WARNING)
                .isNotSuccessful()
                .hasColor(Result.UNSTABLE.color);
        assertThat(Status.FAILED)
                .isNotSuccessful()
                .hasColor(Result.FAILURE.color);
    }
}