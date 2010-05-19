package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the Doxygen warnings.
 *
 * @author Bruno Matos
 */
public class DoxygenParser extends RegexpLineParser {
    /** A Doxygen warning. */
    static final String WARNING_CATEGORY = "Doxygen warning";
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "doxygen";
    /** Pattern of doxygen warnings. */
    private static final String DOXYGEN_WARNING_PATTERN = 
        "^(.+?):(\\d+):(?:\\d+:)? (Warning:) (.*)(is not documented.)$";
    /**
     * Creates a new instance of <code>DoxygenParser</code>.
     */
    public DoxygenParser() {
        super(DOXYGEN_WARNING_PATTERN, "Doxygen");
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(1);
        int lineNumber = getLineNumber(matcher.group(2));
        String message = matcher.group(4) + matcher.group(5);
        
        return new Warning(fileName, lineNumber, WARNING_TYPE,
            WARNING_CATEGORY, message, Priority.NORMAL);
    }
}
