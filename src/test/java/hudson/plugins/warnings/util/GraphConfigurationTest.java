package hudson.plugins.warnings.util;

import static junit.framework.Assert.*;

import org.junit.Test;

/**
 * Tests the class {@link GraphConfiguration}.
 *
 * @author Ulli Hafner
 */
public class GraphConfigurationTest {
    /** Error message. */
    private static final String INVALID_CONFIGURATION_ACCEPTED = "Invalid configuration accepted.";
    /** Valid width. */
    private static final int WIDTH = 50;
    /** Valid height. */
    private static final int HEIGHT = 100;

    /**
     * Ensures that invalid string values are rejected.
     */
    @Test
    public void testInvalidConfiguations() {
        assertInvalidInitializationValue("");
        assertInvalidInitializationValue("111:");
        assertInvalidInitializationValue(null);
        assertInvalidInitializationValue("111:111:");
        assertInvalidInitializationValue("111:111:HELP");
        assertInvalidInitializationValue("50:50:NEW_VS_FIXED:1");
        assertInvalidInitializationValue("NEW:50:NEW_VS_FIXED");
        assertInvalidInitializationValue("50.1:50:NEW_VS_FIXED");
    }

    /**
     * Asserts that the provided initialization value is correctly rejected and
     * the configuration is initialized by default values.
     *
     * @param initializationValue
     *            initialization value
     */
    private void assertInvalidInitializationValue(final String initializationValue) {
        GraphConfiguration configuration = new GraphConfiguration(initializationValue);
        assertTrue(INVALID_CONFIGURATION_ACCEPTED, configuration.isDefault());
    }

    /**
     * Ensures that valid string values are correctly parsed.
     */
    @Test
    public void testValidConfiguations() {
        assertValidConfiguation("50:100:NEW_VS_FIXED", WIDTH, HEIGHT, GraphType.NEW_VS_FIXED);
        assertValidConfiguation("50:100:PRIORITY", WIDTH, HEIGHT, GraphType.PRIORITY);
        assertValidConfiguation("50:100:NONE", WIDTH, HEIGHT, GraphType.NONE);

        GraphConfiguration configuration = new GraphConfiguration(null);
        assertValidConfiguation(configuration.serializeToString(WIDTH, HEIGHT, GraphType.NONE), WIDTH, HEIGHT, GraphType.NONE);
    }

    /**
     * Ensures that the specified string value is correctly parsed.
     *
     * @param initialization
     *            the initialization value
     * @param expectedWidth
     *            the expected width
     * @param expectedHeight
     *            the expected height
     * @param expectedType
     *            the expected type
     */
    private void assertValidConfiguation(final String initialization, final int expectedWidth, final int expectedHeight, final GraphType expectedType) {
        GraphConfiguration configuration = new GraphConfiguration(initialization);
        assertFalse(INVALID_CONFIGURATION_ACCEPTED, configuration.isDefault());
        assertEquals("Wrong width.", expectedWidth, configuration.getWidth());
        assertEquals("Wrong height.", expectedHeight, configuration.getHeight());
        assertSame("Wrong type.", expectedType, configuration.getGraphType());

        if (expectedType != GraphType.NONE) {
            assertTrue("Graph is not visible.", configuration.isVisible());
        }
        else {
            assertFalse("Graph is visible.", configuration.isVisible());
        }
    }
}


