package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for armcc compiler warnings.
 *
 * @author Emanuele Zattin
 */
public class ArmccCompilerParser extends RegexpLineParser {
    /** A ARMCC error. */
    static final String WARNING_CATEGORY = "Armcc Error";
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "armcc";
    /** Pattern of armcc compiler warnings. */
    private static final String ARMCC_WARNING_PATTERN = "^\"(.+)\", line (\\d+): ([A-Z][a-z]+):\\D*(\\d+)\\D*?:\\s+(.+)$";

    /**
     * Creates a new instance of <code>ArmccCompilerParser</code>.
     */
    public ArmccCompilerParser() {
        super(ARMCC_WARNING_PATTERN, "Armcc");
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(1);
        int lineNumber = getLineNumber(matcher.group(2));
        String type = matcher.group(3);
        int errorCode = getLineNumber(matcher.group(4));
        String message = matcher.group(5);
        Priority priority;

        if ("error".equalsIgnoreCase(type)) {
            priority = Priority.HIGH;
        }
        else {
            priority = Priority.NORMAL;
        }

        return new Warning(fileName, lineNumber, WARNING_TYPE, WARNING_CATEGORY, errorCode + " - " + message, priority);
    }
}

