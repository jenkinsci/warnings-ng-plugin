package hudson.plugins.analysis.graph;

import org.junit.Test;

import com.google.common.collect.Sets;

import net.sf.json.JSONObject;
import static org.junit.Assert.*;

/**
 * Tests the class {@link GraphConfiguration}.
 *
 * @author Ulli Hafner
 */
public class GraphConfigurationTest {
    /** Error message. */
    private static final String VALID_CONFIGURATION_NOT_ACCEPTED = "Valid configuration not accepted.";
    /** Valid width. */
    private static final int WIDTH = 50;
    /** Valid height. */
    private static final int HEIGHT = 100;
    /** Valid build count. */
    private static final int BUILDS = 200;
    /** Valid day count. */
    private static final int DAYS = 300;
    /** Valid parameter name. */
    private static final String PARAMETER_NAME = "BUILD_TYPE";
    /** Valid parameter value. */
    private static final String PARAMETER_VALUE = "FULL";

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
        assertInvalidInitializationValue("50!100!200!300!FALSCH");
        assertInvalidInitializationValue("NEW!50!12!13!FIXED!BUILD_TYPE!FULL");
        assertInvalidInitializationValue("50!100!200!300!FIXED!1!!VALUE");
        assertInvalidInitializationValue("50!100!200!300!FIXED!1!NAME!");
    }

    /**
     * Asserts that the provided initialization value is correctly rejected and
     * the configuration is initialized by default values.
     *
     * @param initializationValue
     *            initialization value
     */
    private void assertInvalidInitializationValue(final String initializationValue) {
        GraphConfiguration configuration = createDetailUnderTest();

        assertFalse("Invalid configuration accepted.", configuration.initializeFrom(initializationValue));
        assertTrue("Invalid configuration state.", configuration.isDefault());
    }

    /**
     * Creates the configuration under test.
     *
     * @return the configuration under test
     */
    private GraphConfiguration createDetailUnderTest() {
        return new GraphConfiguration(Sets.newHashSet(new PriorityGraph(), new NewVersusFixedGraph(), new EmptyGraph()));
    }

    /**
     * Ensures that valid string values are correctly parsed.
     */
    @Test
    public void testValidConfiguations() {
        assertValidConfiguation("50!100!200!300!FIXED!1!BUILD_TYPE!FULL", WIDTH, HEIGHT, BUILDS, DAYS, PARAMETER_NAME, PARAMETER_VALUE, NewVersusFixedGraph.class, true);
        assertValidConfiguation("50!100!200!300!PRIORITY!0!BUILD_TYPE!FULL", WIDTH, HEIGHT, BUILDS, DAYS, PARAMETER_NAME, PARAMETER_VALUE,  PriorityGraph.class, false);
        assertValidConfiguation("50!100!200!300!NONE!1!BUILD_TYPE!FULL", WIDTH, HEIGHT, BUILDS, DAYS, PARAMETER_NAME, PARAMETER_VALUE, EmptyGraph.class, true);

        GraphConfiguration configuration = createDetailUnderTest();

        assertTrue(VALID_CONFIGURATION_NOT_ACCEPTED, configuration.initializeFrom("50!100!0!0!NONE!1"));
        assertFalse("Build count is defined but should not.", configuration.isBuildCountDefined());
        assertFalse("Day count is defined but should not.", configuration.isDayCountDefined());

        assertTrue(VALID_CONFIGURATION_NOT_ACCEPTED, configuration.initializeFrom("50!100!2!1!NONE!0"));
        assertTrue("Build count is not defined but should.", configuration.isBuildCountDefined());
        assertTrue("Day count is not defined but should.", configuration.isDayCountDefined());
    }

    /**
     * Ensures that the new boolean property useBuildDate is correctly initialized.
     */
    @Test
    public void testUseBuildDate() {
        assertValidConfiguation("50!100!200!300!FIXED!1!BUILD_TYPE!FULL", WIDTH, HEIGHT, BUILDS, DAYS, PARAMETER_NAME, PARAMETER_VALUE, NewVersusFixedGraph.class, true);
        assertValidConfiguation("50!100!200!300!PRIORITY!0!BUILD_TYPE!FULL", WIDTH, HEIGHT, BUILDS, DAYS, PARAMETER_NAME, PARAMETER_VALUE, PriorityGraph.class, false);

        GraphConfiguration configuration = createDetailUnderTest();

        assertTrue(VALID_CONFIGURATION_NOT_ACCEPTED, configuration.initializeFrom("50!100!0!0!NONE!1"));
        assertTrue("Use build date is defined but should not.", configuration.useBuildDateAsDomain());

        assertTrue(VALID_CONFIGURATION_NOT_ACCEPTED, configuration.initializeFrom("50!100!2!1!NONE!0"));
        assertFalse("Use build date is not defined but should.", configuration.useBuildDateAsDomain());
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
     * @param expectedParameterName
     *            the expected parameter name
     * @param expectedParameterValue
     *            the expected parameter value
     * @param expectedType
     *            the expected type
     * @param expectedUseBuildDate
     *            the expected use build date
     */
    private void assertValidConfiguation(final String initialization, final int expectedWidth, final int expectedHeight,
            final int expectedBuildCount, final int expectedDayCount, final String expectedParameterName, final String expectedParameterValue,
            final Class<? extends BuildResultGraph> expectedType, final boolean expectedUseBuildDate) {
        GraphConfiguration configuation = assertValidConfiguation(initialization, expectedWidth, expectedHeight, expectedBuildCount, expectedDayCount,
                expectedParameterName, expectedParameterValue, expectedType);
        assertEquals("Wrong value for useBuildDate", expectedUseBuildDate, configuation.useBuildDateAsDomain());
    }

    /**
     * Ensures that a valid JSON configuration is correctly parsed.
     */
    @Test
    public void testValidJSONConfiguations() {
        String enabled = "{\"\":\"\",\"buildCountString\":\"" + BUILDS
                + "\",\"dayCountString\":\"" + DAYS
                + "\",\"parameterName\":\"" + PARAMETER_NAME
                + "\",\"parameterValue\":\"" + PARAMETER_VALUE
                + "\",\"graphType\":\"FIXED\",\"height\":\"" + HEIGHT + "\",\"width\":\"" + WIDTH + "\",\"useBuildDateAsDomain\":\"" + true + "\"}";
        JSONObject jsonObject = JSONObject.fromObject(enabled);

        GraphConfiguration configuration = createDetailUnderTest();
        assertTrue(VALID_CONFIGURATION_NOT_ACCEPTED, configuration.initializeFrom(jsonObject));
        verifyConfiguration(WIDTH, HEIGHT, BUILDS, DAYS, PARAMETER_NAME, PARAMETER_VALUE, NewVersusFixedGraph.class, configuration);
        assertTrue(VALID_CONFIGURATION_NOT_ACCEPTED, configuration.useBuildDateAsDomain());
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
     * @return the created configuration
     */
    private GraphConfiguration assertValidConfiguation(final String initialization, final int expectedWidth, final int expectedHeight,
            final int expectedBuildCount, final int expectedDayCount, final String expectedParameterName, final String expectedParameterValue,
            final Class<? extends BuildResultGraph> expectedType) {
        GraphConfiguration configuration = createDetailUnderTest();
        assertTrue(VALID_CONFIGURATION_NOT_ACCEPTED, configuration.initializeFrom(initialization));

        verifyConfiguration(expectedWidth, expectedHeight, expectedBuildCount, expectedDayCount,
                expectedParameterName, expectedParameterValue, expectedType, configuration);

        return configuration;
    }

    /**
     * Verifies the configuration values.
     *
     * @param expectedWidth
     *            expected width
     * @param expectedHeight
     *            expected height
     * @param expectedBuildCount
     *            expected build count
     * @param expectedDayCount
     *            expected day count
     * @param expectedType
     *            expected type of graph
     * @param configuration
     *            the actual configuration to verify
     */
    private void verifyConfiguration(final int expectedWidth, final int expectedHeight,
            final int expectedBuildCount, final int expectedDayCount,
            final String expectedParameterName, final String expectedParameterValue,
            final Class<? extends BuildResultGraph> expectedType, final GraphConfiguration configuration) {
        assertFalse("Valid configuration is not accepted.", configuration.isDefault());
        assertEquals("Wrong width.", expectedWidth, configuration.getWidth());
        assertEquals("Wrong height.", expectedHeight, configuration.getHeight());
        assertEquals("Wrong build counter.", expectedBuildCount, configuration.getBuildCount());
        assertEquals("Wrong day counter.", expectedDayCount, configuration.getDayCount());
        assertEquals("Wrong parameter name.", expectedParameterName, configuration.getParameterName());
        assertEquals("Wrong parameter value.", expectedParameterValue, configuration.getParameterValue());
        assertSame("Wrong type.", expectedType, configuration.getGraphType().getClass());

        if (expectedType == EmptyGraph.class) {
            assertFalse("Graph is visible.", configuration.isVisible());
        }
        else {
            assertTrue("Graph is not visible.", configuration.isVisible());
        }

        String serialized = configuration.serializeToString();
        GraphConfiguration other = createDetailUnderTest();
        assertTrue(VALID_CONFIGURATION_NOT_ACCEPTED, other.initializeFrom(serialized));
        assertEquals("Serialize did not work.", other, configuration);
    }
}
