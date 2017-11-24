package io.jenkins.plugins.analysis.core.quality;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Result;

/**
 * Tests the class {@link QualityGateEnforcer}.
 *
 * @author Ullrich Hafner
 */
class QualityGateEnforcerTest {
    @Test
    void shouldBeSuccessfulWhenNoIssuesPresentAndNoQualityGateIsSet() {
        QualityGateEnforcer enforcer = new QualityGateEnforcer();
        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        QualityGate qualityGate = new QualityGate.QualitiyGateBuilder().build();

        Result result = enforcer.evaluate(run, qualityGate);

        assertThat(result).as("No issues and no quality gate should always be a SUCCESS").isEqualTo(Result.SUCCESS);
    }

    @Test
    void shouldBeSuccessfulWhenNoIssuesPresentAndFailureQualityGateIsSet() {
        QualityGateEnforcer enforcer = new QualityGateEnforcer();
        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        QualityGate qualityGate = new QualityGate.QualitiyGateBuilder().setUnstableThreshold(1, 0, 0, 0).build();

        Result result = enforcer.evaluate(run, qualityGate);

        assertThat(result).as("No issues and failure quality gate should always be a SUCCESS").isEqualTo(Result.SUCCESS);
    }

    @Test
    void shouldFailBuildIfFailureThresholdIsSetHighPriority() {
        QualityGateEnforcer enforcer = new QualityGateEnforcer();
        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        when(run.getTotalSize()).thenReturn(1);
        when(run.getTotalHighPrioritySize()).thenReturn(1);

        QualityGate qualityGate = new QualityGate.QualitiyGateBuilder().setUnstableThreshold(1, 0, 0, 0).build();

        Result result = enforcer.evaluate(run, qualityGate);

        assertThat(result).as("One issue should return FAILURE").isEqualTo(Result.FAILURE);
    }

    @Test
    void shouldFailBuildIfFailureThresholdIsSetNormalPriority() {
        QualityGateEnforcer enforcer = new QualityGateEnforcer();
        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        when(run.getTotalSize()).thenReturn(1);
        when(run.getTotalNormalPrioritySize()).thenReturn(1);

        QualityGate qualityGate = new QualityGate.QualitiyGateBuilder().setUnstableThreshold(1, 0, 0, 0).build();

        Result result = enforcer.evaluate(run, qualityGate);

        assertThat(result).as("One issue should return FAILURE").isEqualTo(Result.FAILURE);
    }

    @Test
    void shouldFailBuildIfFailureThresholdIsSetLowPriority() {
        QualityGateEnforcer enforcer = new QualityGateEnforcer();
        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        when(run.getTotalSize()).thenReturn(1);
        when(run.getTotalLowPrioritySize()).thenReturn(1);

        QualityGate qualityGate = new QualityGate.QualitiyGateBuilder().setUnstableThreshold(1, 0, 0, 0).build();

        Result result = enforcer.evaluate(run, qualityGate);

        assertThat(result).as("One issue should return FAILURE").isEqualTo(Result.FAILURE);
    }

    @Test
    void shouldBeSuccessfulWhenFailureThresholdNormalPriorityIsSetAndTotalLowPriorityIsGreaterThreshold() {
        QualityGateEnforcer enforcer = new QualityGateEnforcer();
        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        when(run.getTotalSize()).thenReturn(3);
        when(run.getTotalLowPrioritySize()).thenReturn(3);

        QualityGate qualityGate = new QualityGate.QualitiyGateBuilder().setUnstableThreshold(5, 1, 2, 4).build();

        Result result = enforcer.evaluate(run, qualityGate);

        assertThat(result).as("One issue should return SUCCESS").isEqualTo(Result.SUCCESS);
    }

    @Test
    void shouldBeSuccessfulWhenFailureThresholdHighPriorityIsSetAndTotalNormalPriorityIsGreaterThreashold() {
        QualityGateEnforcer enforcer = new QualityGateEnforcer();
        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        when(run.getTotalSize()).thenReturn(2);
        when(run.getTotalNormalPrioritySize()).thenReturn(2);

        QualityGate qualityGate = new QualityGate.QualitiyGateBuilder().setUnstableThreshold(5, 1, 3, 4).build();

        Result result = enforcer.evaluate(run, qualityGate);

        assertThat(result).as("One issue should return SUCCESS").isEqualTo(Result.SUCCESS);
    }

    @Test
    void shouldBeSuccessfulWhenFailureThresholdTotalIsSetAndTotalHighPriorityIsSmaller() {
        QualityGateEnforcer enforcer = new QualityGateEnforcer();
        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        when(run.getTotalSize()).thenReturn(1);
        when(run.getTotalHighPrioritySize()).thenReturn(1);

        QualityGate qualityGate = new QualityGate.QualitiyGateBuilder().setUnstableThreshold(5, 2, 3, 4).build();

        Result result = enforcer.evaluate(run, qualityGate);

        assertThat(result).as("One issue should return SUCCESS").isEqualTo(Result.SUCCESS);
    }


}
