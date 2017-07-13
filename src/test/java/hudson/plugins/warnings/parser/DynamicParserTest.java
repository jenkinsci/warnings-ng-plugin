package hudson.plugins.warnings.parser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.google.common.collect.Lists;

import static org.junit.Assert.*;

import hudson.plugins.analysis.util.StringPluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Test the class {@link DynamicParser}.
 *
 * @author Ulli Hafner
 */
public class DynamicParserTest extends PhpParserTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    private static final String TYPE = "PHP Runtime";
    private static final String NEW_LINE = System.getProperty("line.separator");

    @Override
    protected AbstractWarningsParser createParser() {
        return new DynamicParser(TYPE,
                "^.*(PHP Warning|PHP Notice|PHP Fatal error|PHP Parse error):\\s+(.+ in (.+) on line (\\d+))$",
                "        import hudson.plugins.analysis.util.model.Priority;\n"
              + "        import hudson.plugins.warnings.parser.Warning\n"
              + "        String category = matcher.group(1);\n"
              + "        String message = matcher.group(2);\n"
              + "        String fileName = matcher.group(3);\n"
              + "        String start = matcher.group(4);\n"
              + "        Priority priority = Priority.NORMAL;\n"
              + "        if (category.contains(\"Fatal\") || category.contains(\"Parse\")) {\n"
              + "            priority = Priority.HIGH;\n"
              + "        }\n"
              + "        return new Warning(fileName, Integer.parseInt(start), \"PHP Runtime\", category, message, priority);\n",
              TYPE, TYPE);
    }

    /**
     * Parses a file with 9 warnings of a custom parser.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-12280">Issue 12280</a>
     */
    @Test
    public void issue12280() throws IOException {
        Collection<FileAnnotation> warnings = new DynamicParser("issue12280",
                "^.*: XmlDoc warning (\\w+): (.* type ([^\\s]+)\\..*)$",
                "import hudson.plugins.warnings.parser.Warning\n"
                + "    String fileName = matcher.group(3)\n"
                + "    String category = matcher.group(1)\n"
                + "    String message = matcher.group(2)\n"
                + "    return new Warning(fileName, 0, \"Xml Doc\", category, message);", TYPE, TYPE)
                .parse(openFile("issue12280.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 9, warnings.size());
    }

    /**
     * Parses a file with 9 warnings of a custom parser. Should show all line numbers correctly.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-21569">Issue 21569</a>
     */
    @Test
    public void issue21569() throws IOException {
        Collection<FileAnnotation> warnings = new DynamicParser("issue12280",
                "^.*: XmlDoc warning (\\w+): (.* type ([^\\s]+)\\..*)$",
                "import hudson.plugins.warnings.parser.Warning\n"
                + "    String fileName = matcher.group(3)\n"
                + "    String category = matcher.group(1)\n"
                + "    String message = matcher.group(2)\n"
                + "    return new Warning(fileName, lineNumber, \"Xml Doc\", category, message);", TYPE, TYPE)
                .parse(openFile("issue12280.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 9, warnings.size());

        int lineNumber = 2;
        for (FileAnnotation warning : warnings) {
            assertEquals("Wrong line number parsed", lineNumber++, warning.getPrimaryLineNumber());
        }
    }

    /**
     * Parses a file with several warnings from a custom parser.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-11926">Issue 11926</a>
     */
    @Test
    public void issue11926() throws IOException {
        Collection<FileAnnotation> warnings = createCustomParser().parse(openFile("issue11926.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 4, warnings.size());
    }

    private DynamicParser createCustomParser() {
        return new DynamicParser("issue11926",
                "FLMSG (\\S+)\\s+([0-9]+) ([^\\s,]+),([0-9]+) (\\S+)",
                "import hudson.plugins.warnings.parser.Warning\n"
                        + "String fileName = matcher.group(3)\n"
                        + "String code = matcher.group(2)\n"
                        + "String lineNumber = matcher.group(4)\n"
                        + "String type = matcher.group(1)\n"
                        + "String message = matcher.group(5)\n"
                        + "return new Warning(fileName, Integer.parseInt(lineNumber), type, code, message);", TYPE, TYPE);
    }

    /**
     * Parses a file with several warnings from a custom parser.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-11926">Issue 11926</a>
     */
    @Test
    public void issueReadFromFile() throws IOException {
        File file = File.createTempFile("warnings", "test");
        FileUtils.writeStringToFile(file, IOUtils.toString(openFile("issue11926.txt")), "UTF-8");

        DynamicParser dynamicParser = createCustomParser();
        ParserRegistry registry = new ParserRegistry(Lists.newArrayList(dynamicParser), "UTF-8");

        file.deleteOnExit();

        StringPluginLogger logger = new StringPluginLogger("warnings");
        Collection<FileAnnotation> warnings = registry.parse(file, logger);

        assertEquals("Wrong logging message", "[warnings] issue11926 : Found 4 warnings." + NEW_LINE, logger.toString());
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
    }



    @Override
    protected String getWarningsFile() {
        return "php.txt";
    }
}

