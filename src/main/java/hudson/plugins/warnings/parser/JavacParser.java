package hudson.plugins.warnings.parser;

import hudson.Extension;

import java.util.regex.Matcher;

/**
 * A parser for the javac compiler warnings.
 *
 * @author Ulli Hafner
 */
@Extension
public class JavacParser extends RegexpLineParser {
    private static final long serialVersionUID = 7199325311690082782L;
    private static final String JAVAC_WARNING_PATTERN = "^(?:\\[WARNING\\]\\s+)?(.*):\\[(\\d*)[.,; 0-9]*\\]\\s*(?:\\[(\\w*)\\])?\\s*(.*)$";

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
    protected String getId() {
        return "Java Compiler"; // old ID in serialization
    }
}

