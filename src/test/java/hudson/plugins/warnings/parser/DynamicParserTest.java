package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

/**
 * Test the class {@link DynamicParser}.
 *
 * @author Ulli Hafner
 */
public class DynamicParserTest extends PhpParserTest {
    /** {@inheritDoc} */
    @Override
    protected WarningsParser createParser() {
        return new DynamicParser("PHP Runtime Warning",
                "^.*(PHP Warning|PHP Notice|PHP Fatal error):\\s+(.+ in (.+) on line (\\d+))$",
                "        import hudson.plugins.analysis.util.model.Priority;\n"
              + "        import hudson.plugins.warnings.parser.Warning\n"
              + "        String category = matcher.group(1);\n"
              + "        String message = matcher.group(2);\n"
              + "        String fileName = matcher.group(3);\n"
              + "        String start = matcher.group(4);\n"
              + "        Priority priority = Priority.NORMAL;\n"
              + "        if (category.contains(\"Fatal\")) {\n"
              + "            priority = Priority.HIGH;\n"
              + "        }\n"
              + "        return new Warning(fileName, Integer.parseInt(start), \"PHP Runtime Warning\", category, message, priority);\n");
    }


    /**
     * Parses a file with one warning that are started by ant.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-9926">Issue 9926</a>
     */
    @Test
    public void issue12280() throws IOException {
        Collection<FileAnnotation> warnings = new DynamicParser("issue12280",
                "^.*: XmlDoc warning (\\w+): (.* type ([^\\s]+)\\..*)$",
                "import hudson.plugins.warnings.parser.Warning\n"
                + "    String fileName = matcher.group(3)\n"
                + "    String category = matcher.group(1)\n"
                + "    String message = matcher.group(2)\n"
                + "    return new Warning(fileName, 0, \"Xml Doc\", category, message);")
                .parse(openFile("issue12280.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 9, warnings.size());
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "php.txt";
    }
}

