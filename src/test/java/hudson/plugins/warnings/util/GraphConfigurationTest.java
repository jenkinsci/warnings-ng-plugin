package hudson.plugins.warnings.util;

import static junit.framework.Assert.*;

import org.junit.Test;

/**
 * Tests the class {@link GraphConfiguration}.
 *
 * @author Ulli Hafner
 */
public class GraphConfigurationTest {
    /** Valid width. */
    private static final int WIDTH = 50;
    /** Valid height. */
    private static final int HEIGHT = 100;
    /** Valid build count. */
    private static final int BUILDS = 200;
    /** Valid day count. */
    private static final int DAYS = 300;

    /**
     * Ensures that invalid string values are rejected.
     */
    @Test
    public void testInvalidConfiguations() {
        assertInvalidInitializationValue("");
        assertInvalidInitializationValue("111!");
        assertInvalidInitializationValue(null);
        assertInvalidInitializationValue("111!111!");
        assertInvalidInitializationValue("111!111!HELP");
        assertInvalidInitializationValue("50!50!FIXED!1");
        assertInvalidInitializationValue("NEW!50!12!13!FIXED");
        assertInvalidInitializationValue("50.1!50!12!13!FIXED");
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
        assertTrue("Invalid configuration accepted.", configuration.isDefault());
    }

    /**
     * Ensures that valid string values are correctly parsed.
     */
    @Test
    public void testValidConfiguations() {
        assertValidConfiguation("50!100!200!300!FIXED", WIDTH, HEIGHT, BUILDS, DAYS, GraphType.FIXED);
        assertValidConfiguation("50!100!200!300!PRIORITY", WIDTH, HEIGHT, BUILDS, DAYS, GraphType.PRIORITY);
        assertValidConfiguation("50!100!200!300!NONE", WIDTH, HEIGHT, BUILDS, DAYS, GraphType.NONE);

        GraphConfiguration configuration = new GraphConfiguration(null);
        assertValidConfiguation(configuration.serializeToString(WIDTH, HEIGHT, BUILDS, DAYS, GraphType.NONE),
                WIDTH, HEIGHT, BUILDS, DAYS, GraphType.NONE);

        configuration = new GraphConfiguration("50!100!0!0!NONE");
        assertFalse("Build count is defined but should not.", configuration.isBuildCountDefined());
        assertFalse("Day count is defined but should not.", configuration.isDayCountDefined());

        configuration = new GraphConfiguration("50!100!2!1!NONE");
        assertTrue("Build count is not defined but should.", configuration.isBuildCountDefined());
        assertTrue("Day count is not defined but should.", configuration.isDayCountDefined());
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
     * @param expectedBuildCount
     *            the expected number of builds
     * @param expectedDayCount
     *            the expected number of days
     * @param expectedType
     *            the expected type
     */
    private void assertValidConfiguation(final String initialization, final int expectedWidth, final int expectedHeight, final int expectedBuildCount, final int expectedDayCount, final GraphType expectedType) {
        GraphConfiguration configuration = new GraphConfiguration(initialization);
        assertFalse("Valid configuration is not accepted.", configuration.isDefault());
        assertEquals("Wrong width.", expectedWidth, configuration.getWidth());
        assertEquals("Wrong height.", expectedHeight, configuration.getHeight());
        assertEquals("Wrong build counter.", expectedBuildCount, configuration.getBuildCount());
        assertEquals("Wrong day counter.", expectedDayCount, configuration.getDayCount());
        assertSame("Wrong type.", expectedType, configuration.getGraphType());

        if (expectedType != GraphType.NONE) {
            assertTrue("Graph is not visible.", configuration.isVisible());
        }
        else {
            assertFalse("Graph is visible.", configuration.isVisible());
        }
    }
}


