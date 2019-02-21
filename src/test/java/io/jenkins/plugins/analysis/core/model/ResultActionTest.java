package io.jenkins.plugins.analysis.core.model;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.SerializableTest;

import io.jenkins.plugins.analysis.core.util.HealthDescriptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Run;

/**
 * Tests the class {@link ResultAction}.
 *
 * @author Ullrich Hafner
 */
class ResultActionTest extends SerializableTest<ResultAction> {
    @Test
    void shouldRestoreRun() {
        ResultAction action = createSerializable();

        assertThat(action.getOwner()).isNull();

        Run run = mock(Run.class);
        action.onAttached(run);
        assertThat(action.getOwner()).isSameAs(run);
    }

    @Override
    protected ResultAction createSerializable() {
        return new ResultAction(null, mock(AnalysisResult.class),
                new HealthDescriptor(0, 0, Severity.WARNING_HIGH),
                "ID", "Name", StandardCharsets.UTF_8);
    }
}