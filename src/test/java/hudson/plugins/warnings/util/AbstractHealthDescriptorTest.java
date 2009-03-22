package hudson.plugins.warnings.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

/**
 * Abstract test case for {@link AbstractHealthDescriptor}.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractHealthDescriptorTest extends AbstractEnglishLocaleTest {
    /**
     * Tests the method {@link AbstractHealthDescriptor#isThresholdEnabled()}.
     */
    @Test
    public void testThresholds() {
        assertTrue(createHealthDescriptor("0", "", "").isThresholdEnabled());
        assertTrue(createHealthDescriptor("1", "", "").isThresholdEnabled());
        assertTrue(createHealthDescriptor("100", "", "").isThresholdEnabled());

        assertFalse(createHealthDescriptor("-1", "", "").isThresholdEnabled());
        assertFalse(createHealthDescriptor("", "", "").isThresholdEnabled());
        assertFalse(createHealthDescriptor(null, "", "").isThresholdEnabled());
    }

    /**
     * Tests the method {@link AbstractHealthDescriptor#isThresholdEnabled()}.
     */
    @Test
    public void testHealthyThresholds() {
        assertTrue(createHealthDescriptor("", "0", "1").isHealthyReportEnabled());
        assertTrue(createHealthDescriptor("", "1", "2").isHealthyReportEnabled());
        assertTrue(createHealthDescriptor("", "10", "20").isHealthyReportEnabled());

        assertFalse(createHealthDescriptor("", "0", "0").isHealthyReportEnabled());
        assertFalse(createHealthDescriptor("", "1", "1").isThresholdEnabled());
        assertFalse(createHealthDescriptor("", "2", "1").isThresholdEnabled());
        assertFalse(createHealthDescriptor("", "2", "").isThresholdEnabled());
        assertFalse(createHealthDescriptor("", "", "2").isThresholdEnabled());
        assertFalse(createHealthDescriptor("", null, "2").isThresholdEnabled());
    }

    /**
     * Tests the method {@link AbstractHealthDescriptor#getMinimumAnnotations}.
     */
    @Test
    public void testConversionOfThresholds() {
        assertEquals(0, createHealthDescriptor("0", "", "").getMinimumAnnotations());
        assertEquals(1, createHealthDescriptor("1", "", "").getMinimumAnnotations());
        assertEquals(100, createHealthDescriptor("100", "", "").getMinimumAnnotations());
    }


    /**
     * Tests the method {@link AbstractHealthDescriptor#getHealthyAnnotations()}
     * and {@link AbstractHealthDescriptor#getUnHealthyAnnotations()}.
     */
    @Test
    public void testConversionOfHealthiness() {
        assertEquals(1, createHealthDescriptor("0", "1", "2").getHealthyAnnotations());
        assertEquals(2, createHealthDescriptor("0", "1", "2").getUnHealthyAnnotations());
    }

    /**
     * Tests the method {@link AbstractHealthDescriptor#getMinimumAnnotations}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyContractOfThreshold() {
        createHealthDescriptor("-1", "0", "0").getMinimumAnnotations();
    }

    /**
     * Tests the method {@link AbstractHealthDescriptor#getHealthyAnnotations()}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyContractOfHealthy() {
        createHealthDescriptor("0", "-1", "0").getHealthyAnnotations();
    }

    /**
     * Tests the method {@link AbstractHealthDescriptor#getHealthyAnnotations()}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyContractOfUnHealthy() {
        createHealthDescriptor("0", "0", "-1").getUnHealthyAnnotations();
    }

    /**
     * Create a health descriptor mock that should be used as a basis for the
     * concrete {@link AbstractHealthDescriptor} sub type.
     *
     * @param threshold
     *            Annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param healthy
     *            Report health as 100% when the number of open tasks is less
     *            than this value
     * @param unHealthy
     *            Report health as 0% when the number of open tasks is greater
     *            than this value
     * @return the descriptor under test
     */
    private AbstractHealthDescriptor createHealthDescriptor(final String threshold, final String healthy, final String unHealthy) {
        HealthDescriptor healthDescriptor = mock(HealthDescriptor.class);
        when(healthDescriptor.getThreshold()).thenReturn(threshold);
        when(healthDescriptor.getHealthy()).thenReturn(healthy);
        when(healthDescriptor.getUnHealthy()).thenReturn(unHealthy);

        return createHealthDescriptor(healthDescriptor);
    }

    /**
     * Factory method to create the health descriptor under test.
     *
     * @param healthDescriptor
     *            the basis health descriptor mock
     * @return the descriptor under test
     */
    protected abstract AbstractHealthDescriptor createHealthDescriptor(HealthDescriptor healthDescriptor);
}

