package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.SerializableTest;

import hudson.model.Item;

import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateDescriptor;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link QualityGate}.
 *
 * @author Ullrich Hafner
 */
class QualityGateTest extends SerializableTest<QualityGate> {
    @Test
    void shouldValidateThreshold() {
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.hasPermission(Item.CONFIGURE, null)).thenReturn(true);

        QualityGateDescriptor descriptor = new QualityGateDescriptor(jenkinsFacade);

        assertThat(descriptor.doCheckThreshold(null, 0))
                .isError()
                .hasMessage(Messages.FieldValidator_Error_NegativeThreshold());
        assertThat(descriptor.doCheckThreshold(null, -1))
                .isError()
                .hasMessage(Messages.FieldValidator_Error_NegativeThreshold());

        assertThat(descriptor.doCheckThreshold(null, 1))
                .isOk();
    }

    @Override
    protected QualityGate createSerializable() {
        return new QualityGate(1, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
    }
}
