package hudson.plugins.analysis.core;

import static org.junit.Assert.*;
import net.sf.json.JSONObject;

import org.junit.Test;

/**
 * Tests the class {@link PluginDescriptor}.
 *
 * @author Ulli Hafner
 */
public class PluginDescriptorTest {
    /**
     * Verifies that the new structure of request values is flat, e.g. the canComputeNew section is not a sub structure anymore.
     */
    @Test
    public void testWithNewWarnings() {
        String request = "{\"canComputeNew\":{\"failedNewAll\":\"\",\"failedNewHigh\":\"1\",\"failedNewLow\":\"\",\"failedNewNormal\":\"\",\"unstableNewAll\":\"1\",\"unstableNewHigh\":\"\",\"unstableNewLow\":\"\",\"unstableNewNormal\":\"\",\"useDeltaValues\":false},\"canRunOnFailed\":false,\"defaultEncoding\":\"\",\"failedTotalAll\":\"\",\"failedTotalHigh\":\"\",\"failedTotalLow\":\"\",\"failedTotalNormal\":\"\",\"healthy\":\"0\",\"pattern\":\"\",\"shouldDetectModules\":false,\"thresholdLimit\":\"low\",\"unHealthy\":\"50\",\"unstableTotalAll\":\"\",\"unstableTotalHigh\":\"\",\"unstableTotalLow\":\"\",\"unstableTotalNormal\":\"\"}";

        JSONObject input = JSONObject.fromObject(request);
        JSONObject output = PluginDescriptor.convertHierarchicalFormData(input);

        String expected = "{\"canComputeNew\":true,\"failedNewAll\":\"\",\"failedNewHigh\":\"1\",\"failedNewLow\":\"\",\"failedNewNormal\":\"\",\"unstableNewAll\":\"1\",\"unstableNewHigh\":\"\",\"unstableNewLow\":\"\",\"unstableNewNormal\":\"\",\"useDeltaValues\":false,\"canRunOnFailed\":false,\"defaultEncoding\":\"\",\"failedTotalAll\":\"\",\"failedTotalHigh\":\"\",\"failedTotalLow\":\"\",\"failedTotalNormal\":\"\",\"healthy\":\"0\",\"pattern\":\"\",\"shouldDetectModules\":false,\"thresholdLimit\":\"low\",\"unHealthy\":\"50\",\"unstableTotalAll\":\"\",\"unstableTotalHigh\":\"\",\"unstableTotalLow\":\"\",\"unstableTotalNormal\":\"\"}";
        assertEquals("Wrong JSON ", JSONObject.fromObject(expected), output);
    }

    /**
     * Verifies that the structure of a request is not changed if the canComputeNew section is missing.
     */
    @Test
    public void testWithoutNewWarnings() {
        String request = "{\"canRunOnFailed\":false,\"defaultEncoding\":\"\",\"failedTotalAll\":\"\",\"failedTotalHigh\":\"\",\"failedTotalLow\":\"\",\"failedTotalNormal\":\"\",\"healthy\":\"0\",\"pattern\":\"\",\"shouldDetectModules\":false,\"thresholdLimit\":\"low\",\"unHealthy\":\"50\",\"unstableTotalAll\":\"\",\"unstableTotalHigh\":\"\",\"unstableTotalLow\":\"\",\"unstableTotalNormal\":\"\"}";

        JSONObject input = JSONObject.fromObject(request);
        JSONObject output = PluginDescriptor.convertHierarchicalFormData(input);

        assertEquals("Wrong JSON ", input, output);
    }
}

