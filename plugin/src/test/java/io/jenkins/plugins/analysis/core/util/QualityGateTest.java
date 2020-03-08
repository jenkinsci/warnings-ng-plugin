package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.SerializableTest;

import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateDescriptor;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link QualityGate}.
 *
 * @author Ullrich Hafner
 */
class QualityGateTest extends SerializableTest<QualityGate> {
    @Test
    void shouldValidateThreshold() {
        QualityGateDescriptor descriptor = new QualityGateDescriptor();

        assertThat(descriptor.doCheckThreshold(0))
                .isError()
                .hasMessage(Messages.FieldValidator_Error_NegativeThreshold());
        assertThat(descriptor.doCheckThreshold(-1))
                .isError()
                .hasMessage(Messages.FieldValidator_Error_NegativeThreshold());

        assertThat(descriptor.doCheckThreshold(1))
                .isOk();
    }

    @Override
    protected QualityGate createSerializable() {
        return new QualityGate(1, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
    }
}