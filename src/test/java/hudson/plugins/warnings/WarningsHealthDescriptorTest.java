package hudson.plugins.warnings;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import hudson.plugins.warnings.util.AbstractHealthDescriptor;
import hudson.plugins.warnings.util.AbstractHealthDescriptorTest;
import hudson.plugins.warnings.util.HealthDescriptor;
import hudson.plugins.warnings.util.NullHealthDescriptor;
import hudson.plugins.warnings.util.model.AnnotationProvider;

import org.junit.Test;
import org.jvnet.localizer.Localizable;

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
        assertEquals(Messages.Warnings_ResultAction_HealthReportNoItem(), description.toString());

        when(provider.getNumberOfAnnotations()).thenReturn(1);
        description = healthDescriptor.createDescription(provider);
        assertEquals(Messages.Warnings_ResultAction_HealthReportSingleItem(), description.toString());

        when(provider.getNumberOfAnnotations()).thenReturn(2);
        description = healthDescriptor.createDescription(provider);
        assertEquals(Messages.Warnings_ResultAction_HealthReportMultipleItem(2), description.toString());
    }

    /** {@inheritDoc} */
    @Override
    protected AbstractHealthDescriptor createHealthDescriptor(final HealthDescriptor healthDescriptor) {
        return new WarningsHealthDescriptor(healthDescriptor);
    }
}

