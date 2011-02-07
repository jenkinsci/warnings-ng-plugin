package hudson.plugins.warnings.parser;


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

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "php.txt";
    }
}

