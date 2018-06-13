package io.jenkins.plugins.analysis.warnings.groovy;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.Pep8ParserTest;

/**
 * Test the class {@link DynamicLineParser}. Creates a new Pep8 parser in Groovy. All Pep8 test cases are reused.
 *
 * @author Ulli Hafner
 */
class DynamicLineParserTest extends Pep8ParserTest {
    @Override
    public AbstractParser createParser() {
        return new DynamicLineParser("(.*):(\\d+):(\\d+): (\\D\\d*) (.*)", toString("pep8.groovy"));
    }
}

