package hudson.plugins.analysis.graph;

import static junit.framework.Assert.*;
import net.sf.json.JSONObject;

import org.junit.Test;
import org.mortbay.util.ajax.JSON;

import com.google.common.collect.Sets;

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
        assertInvalidInitializationValue("50!100!200!300!FALSCH");
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
        assertValidConfiguation("50!100!200!300!FIXED", WIDTH, HEIGHT, BUILDS, DAYS, NewVersusFixedGraph.class);
        assertValidConfiguation("50!100!200!300!PRIORITY", WIDTH, HEIGHT, BUILDS, DAYS, PriorityGraph.class);
        assertValidConfiguation("50!100!200!300!NONE", WIDTH, HEIGHT, BUILDS, DAYS, EmptyGraph.class);

        GraphConfiguration configuration = createDetailUnderTest();

        assertTrue("Valid configuration not accepted.", configuration.initializeFrom("50!100!0!0!NONE"));
        assertFalse("Build count is defined but should not.", configuration.isBuildCountDefined());
        assertFalse("Day count is defined but should not.", configuration.isDayCountDefined());

        assertTrue("Valid configuration not accepted.", configuration.initializeFrom("50!100!2!1!NONE"));
        assertTrue("Build count is not defined but should.", configuration.isBuildCountDefined());
        assertTrue("Day count is not defined but should.", configuration.isDayCountDefined());
    }

    /**
     * Ensures that a valid JSON configuration is correctly parsed.
     */
    @Test
    public void testValidJSONConfiguations() {
        Object enabled = JSON.parse("{\"\":\"\",\"buildCountString\":\"" + BUILDS
                + "\",\"dayCountString\":\"" + DAYS
                + "\",\"graphType\":\"FIXED\",\"height\":\"" + HEIGHT + "\",\"width\":\"" + WIDTH + "\"}");
        JSONObject jsonObject = JSONObject.fromObject(enabled);

        GraphConfiguration configuration = createDetailUnderTest();
        assertTrue("Valid configuration not accepted.", configuration.initializeFrom(jsonObject));
        verifyConfiguration(WIDTH, HEIGHT, BUILDS, DAYS, NewVersusFixedGraph.class, configuration);
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
    private void assertValidConfiguation(final String initialization, final int expectedWidth, final int expectedHeight,
            final int expectedBuildCount, final int expectedDayCount, final Class<? extends BuildResultGraph> expectedType) {
        GraphConfiguration configuration = createDetailUnderTest();
        assertTrue("Valid configuration not accepted.", configuration.initializeFrom(initialization));

        verifyConfiguration(expectedWidth, expectedHeight, expectedBuildCount, expectedDayCount,
                expectedType, configuration);
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
            final Class<? extends BuildResultGraph> expectedType, final GraphConfiguration configuration) {
        assertFalse("Valid configuration is not accepted.", configuration.isDefault());
        assertEquals("Wrong width.", expectedWidth, configuration.getWidth());
        assertEquals("Wrong height.", expectedHeight, configuration.getHeight());
        assertEquals("Wrong build counter.", expectedBuildCount, configuration.getBuildCount());
        assertEquals("Wrong day counter.", expectedDayCount, configuration.getDayCount());
        assertSame("Wrong type.", expectedType, configuration.getGraphType().getClass());

        if (expectedType == EmptyGraph.class) {
            assertFalse("Graph is visible.", configuration.isVisible());
        }
        else {
            assertTrue("Graph is not visible.", configuration.isVisible());
        }

        String serialized = configuration.serializeToString();
        GraphConfiguration other = createDetailUnderTest();
        assertTrue("Valid configuration not accepted.", other.initializeFrom(serialized));
        assertEquals("Serialize did not work.", other, configuration);
    }
}


