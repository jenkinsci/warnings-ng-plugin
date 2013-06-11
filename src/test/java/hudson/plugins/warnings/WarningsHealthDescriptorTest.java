package hudson.plugins.warnings;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.jvnet.localizer.Localizable;

import hudson.plugins.analysis.core.AbstractHealthDescriptor;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.NullHealthDescriptor;
import hudson.plugins.analysis.test.AbstractHealthDescriptorTest;
import hudson.plugins.analysis.util.model.AnnotationProvider;

/**
 * Tests the class {@link WarningsHealthDescriptor}.
 *
 * @author Ulli Hafner
 */
public class WarningsHealthDescriptorTest extends AbstractHealthDescriptorTest {
    /**
     * Verify number of items.
     */
    @Test
    public void verifyNumberOfItems() {
        AnnotationProvider provider = mock(AnnotationProvider.class);
        WarningsHealthDescriptor healthDescriptor = new WarningsHealthDescriptor(NullHealthDescriptor.NULL_HEALTH_DESCRIPTOR);

        Localizable description = healthDescriptor.createDescription(provider);
        assertEquals(WRONG_DESCRIPTION, Messages.Warnings_ResultAction_HealthReportNoItem(), description.toString());

        when(provider.getNumberOfAnnotations()).thenReturn(1);
        description = healthDescriptor.createDescription(provider);
        assertEquals(WRONG_DESCRIPTION, Messages.Warnings_ResultAction_HealthReportSingleItem(), description.toString());

        when(provider.getNumberOfAnnotations()).thenReturn(2);
        description = healthDescriptor.createDescription(provider);
        assertEquals(WRONG_DESCRIPTION, Messages.Warnings_ResultAction_HealthReportMultipleItem(2), description.toString());
    }

    @Override
    protected AbstractHealthDescriptor createHealthDescriptor(final HealthDescriptor healthDescriptor) {
        return new WarningsHealthDescriptor(healthDescriptor);
    }
}

