package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;

import hudson.model.Result;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the class {@link QualityGateStatus}.
 *
 * @author Ullrich Hafner
 */
class QualityGateStatusTest {
    @Test
    void shouldIdentifySuccessfulStatus() {
        assertThat(QualityGateStatus.PASSED).isSuccessful().hasColor(Result.SUCCESS.color);
        assertThat(QualityGateStatus.INACTIVE).isSuccessful().hasColor(Result.NOT_BUILT.color);
        assertThat(QualityGateStatus.WARNING).isNotSuccessful().hasColor(Result.UNSTABLE.color);
        assertThat(QualityGateStatus.FAILED).isNotSuccessful().hasColor(Result.FAILURE.color);
    }

    @Test
    void shouldDefineOrder() {
        assertThat(QualityGateStatus.FAILED.isWorseThan(QualityGateStatus.INACTIVE)).isTrue();
        assertThat(QualityGateStatus.FAILED.isWorseThan(QualityGateStatus.PASSED)).isTrue();
        assertThat(QualityGateStatus.FAILED.isWorseThan(QualityGateStatus.WARNING)).isTrue();

        assertThat(QualityGateStatus.FAILED.isWorseThan(QualityGateStatus.FAILED)).isFalse();

        assertThat(QualityGateStatus.WARNING.isWorseThan(QualityGateStatus.INACTIVE)).isTrue();
        assertThat(QualityGateStatus.WARNING.isWorseThan(QualityGateStatus.PASSED)).isTrue();

        assertThat(QualityGateStatus.WARNING.isWorseThan(QualityGateStatus.FAILED)).isFalse();
        assertThat(QualityGateStatus.WARNING.isWorseThan(QualityGateStatus.WARNING)).isFalse();

        assertThat(QualityGateStatus.PASSED.isWorseThan(QualityGateStatus.INACTIVE)).isTrue();

        assertThat(QualityGateStatus.PASSED.isWorseThan(QualityGateStatus.PASSED)).isFalse();
        assertThat(QualityGateStatus.PASSED.isWorseThan(QualityGateStatus.FAILED)).isFalse();
        assertThat(QualityGateStatus.PASSED.isWorseThan(QualityGateStatus.WARNING)).isFalse();


        assertThat(QualityGateStatus.INACTIVE.isWorseThan(QualityGateStatus.INACTIVE)).isFalse();
        assertThat(QualityGateStatus.INACTIVE.isWorseThan(QualityGateStatus.PASSED)).isFalse();
        assertThat(QualityGateStatus.INACTIVE.isWorseThan(QualityGateStatus.FAILED)).isFalse();
        assertThat(QualityGateStatus.INACTIVE.isWorseThan(QualityGateStatus.WARNING)).isFalse();
    }
}