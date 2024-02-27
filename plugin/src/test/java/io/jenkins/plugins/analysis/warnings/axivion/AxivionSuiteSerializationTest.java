package io.jenkins.plugins.analysis.warnings.axivion;

import edu.hm.hafner.util.SerializableTest;

class AxivionSuiteSerializationTest extends SerializableTest<AxivionSuite> {
    @Override
    protected AxivionSuite createSerializable() {
        return restore(readAllBytes("axivion-suite-9.0.1.ser"));
    }
}
