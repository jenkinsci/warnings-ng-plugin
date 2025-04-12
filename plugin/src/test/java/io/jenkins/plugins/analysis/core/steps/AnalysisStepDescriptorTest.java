package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep.Descriptor;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link AnalysisStepDescriptor}.
 *
 * @author Ullrich Hafner
 */
class AnalysisStepDescriptorTest {
    @Test
    void shouldPrintArgumentsToString() {
        var descriptor = new Descriptor();

        assertThat(descriptor.argumentsToString(new HashMap<>())).isEqualTo("{}");

        Map<String, Object> singleton = new HashMap<>();
        singleton.put("key", "value");
        assertThat(descriptor.argumentsToString(singleton)).isEqualTo("value");

        Map<String, Object> multiple = new HashMap<>();
        multiple.put("key-1", "value-1");
        multiple.put("key-2", "value-2");
        assertThat(descriptor.argumentsToString(multiple)).isEqualTo("{key-1=value-1, key-2=value-2}");
    }
}
