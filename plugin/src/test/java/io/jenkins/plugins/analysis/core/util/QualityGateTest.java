package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.SerializableTest;

import hudson.model.BuildableItem;
import hudson.model.Item;

import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.WarningsQualityGateDescriptor;
import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.QualityGate;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;

import static io.jenkins.plugins.analysis.core.testutil.FormValidationAssert.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link QualityGate}.
 *
 * @author Ullrich Hafner
 */
class QualityGateTest extends SerializableTest<WarningsQualityGate> {
    @Test
    void shouldValidateThreshold() {
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.hasPermission(Item.CONFIGURE, (BuildableItem) null)).thenReturn(true);

        WarningsQualityGateDescriptor descriptor = new WarningsQualityGateDescriptor(jenkinsFacade);

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
    protected WarningsQualityGate createSerializable() {
        var qualityGate = new WarningsQualityGate(QualityGateType.TOTAL);
        qualityGate.setCriticality(QualityGateCriticality.UNSTABLE);
        qualityGate.setIntegerThreshold(1);
        return qualityGate;
    }
}
