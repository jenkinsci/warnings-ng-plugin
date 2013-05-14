package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.warnings.WarningsDescriptor;

/**
 * A parser for the javac compiler warnings.
 *
 * @author Ulli Hafner
 */
@Extension
public class JavacParser extends RegexpLineParser {
    static final String JAVA_SMALL_ICON = WarningsDescriptor.IMAGE_PREFIX + "java-24x24.png";
    static final String JAVA_LARGE_ICON = WarningsDescriptor.IMAGE_PREFIX + "java-48x48.png";

    private static final long serialVersionUID = 7199325311690082782L;
    private static final String JAVAC_WARNING_PATTERN = "^(?:\\[WARNING\\]\\s+)?([^\\[]*):\\[(\\d+)[.,; 0-9]*\\]\\s*(?:\\[(\\w+)\\])?\\s*(.*)$";

    /**
     * Creates a new instance of {@link JavacParser}.
     */
    public JavacParser() {
        super(Messages._Warnings_JavaParser_ParserName(),
                Messages._Warnings_JavaParser_LinkName(),
                Messages._Warnings_JavaParser_TrendName(),
                JAVAC_WARNING_PATTERN, true);
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("[");
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(4);
        String category = classifyIfEmpty(matcher.group(3), message);

        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), category, message);
    }

    @Override
    public String getSmallImage() {
        return JAVA_SMALL_ICON;
    }

    @Override
    public String getLargeImage() {
        return JAVA_LARGE_ICON;
    }

    @Override
    protected String getId() {
        return "Java Compiler"; // old ID in serialization
    }
}

