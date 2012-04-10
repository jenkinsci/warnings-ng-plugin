package hudson.plugins.warnings.parser;



/**
 * Tests the class {@link DynamicDocumentParser}.
 *
 * @author Ulli Hafner
 */
public class DynamicDocumentParserTest extends EclipseParserTest {
    private static final String TYPE = "Eclipse Dynamic";

    @Override
    protected AbstractWarningsParser createParser() {
        // CHECKSTYLE:OFF
        return new DynamicDocumentParser(TYPE,
                "(WARNING|ERROR)\\s*in\\s*(.*)\\(at line\\s*(\\d+)\\).*(?:\\r?\\n[^\\^]*)+(?:\\r?\\n(.*)([\\^]+).*)\\r?\\n(?:\\s*\\[.*\\]\\s*)?(.*)",
                "import hudson.plugins.warnings.parser.Warning\n" +
                "import org.apache.commons.lang.StringUtils\n" +
                "import hudson.plugins.analysis.util.model.Priority\n" +
                "String type = matcher.group(1)\n" +
                "Priority priority;\n" +
                "if (\"warning\".equalsIgnoreCase(type)) {\n" +
                "    priority = Priority.NORMAL;\n" +
                "}\n" +
                "else {\n" +
                "    priority = Priority.HIGH;\n" +
                "}\n" +
                "String fileName = matcher.group(2)\n" +
                "String lineNumber = matcher.group(3)\n" +
                "String message = matcher.group(6)\n" +
                "Warning warning = new Warning(fileName, Integer.parseInt(lineNumber), \"" + TYPE + "\", \"\", message);\n" +
                "\n" +
                "int columnStart = StringUtils.defaultString(matcher.group(4)).length();\n" +
                "int columnEnd = columnStart + matcher.group(5).length();\n" +
                "warning.setColumnPosition(columnStart, columnEnd);\n" +
                "\n" +
                "        return warning;\n", TYPE, TYPE);
        // CHECKSTYLE:ON
    }

    @Override
    protected String getType() {
        return TYPE;
    }
}

