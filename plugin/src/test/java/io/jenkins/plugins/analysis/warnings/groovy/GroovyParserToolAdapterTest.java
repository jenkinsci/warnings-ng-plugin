package io.jenkins.plugins.analysis.warnings.groovy;

import edu.hm.hafner.util.SerializableTest;

/**
 * Tests the class {@link GroovyParserToolAdapter}.
 *
 * @author Ullrich Hafner
 */
class GroovyParserToolAdapterTest extends SerializableTest<GroovyParserToolAdapter> {
    @Override
    protected GroovyParserToolAdapter createSerializable() {
        GroovyParser parser = new GroovyParser("id", "name", "regexp", "script", "example");
        
        return new GroovyParserToolAdapter(parser);
    }
}