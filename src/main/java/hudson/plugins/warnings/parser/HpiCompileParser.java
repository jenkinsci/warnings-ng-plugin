package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.FileAnnotation;

import java.util.regex.Matcher;

/**
 * A parser for the maven-hpi-plugin compiler warnings.
 */
public class HpiCompileParser extends RegexpParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Maven HPI Plugin";
    /** Pattern of hpi compiler warnings. */
    private static final String HPI_WARNING_PATTERN = "(.*\\.java):(\\d*):.*\\[(.*)\\]\\s*(.*)";

    /**
     * Creates a new instance of <code>HpiCompileParser</code>.
     */
    public HpiCompileParser() {
        super(HPI_WARNING_PATTERN);
    }

    /** {@inheritDoc} */
    @Override
    protected FileAnnotation createWarning(final Matcher matcher) {
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, matcher.group(3), matcher.group(4));
    }
}

