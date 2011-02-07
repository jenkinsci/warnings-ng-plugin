package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the tnsdl translator warnings.
 *
 * @author Shaohua Wen
 */
public class TnsdlParser extends RegexpLineParser {
    /** A TNSDL Translator error. */
    static final String WARNING_CATEGORY = "Tnsdl Error";
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "TNSDL Translator";
    /** Pattern of tnsdl translator warnings. */
    private static final String TNSDL_WARNING_PATTERN = "^tnsdl((.*)?):\\(.*\\) (.*) \\((.*)\\):(.*)$";

    /**
     * Creates a new instance of <code>TnsdlParser</code>.
     */
    public TnsdlParser() {
        super(TNSDL_WARNING_PATTERN, WARNING_TYPE, true);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("tnsdl");
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher
     *            the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(3);
        int lineNumber = getLineNumber(matcher.group(4));
        String message = matcher.group(5);
        Priority priority;

        if (matcher.group().contains("(E)")) {
            priority = Priority.HIGH;
        }
        else {
            priority = Priority.NORMAL;
        }

        return new Warning(fileName, lineNumber, WARNING_TYPE, WARNING_CATEGORY, message, priority);
    }
}

