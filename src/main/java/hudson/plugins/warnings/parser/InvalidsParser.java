package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for Oracle Invalids.
 *
 * @author Ulli Hafner
 */
public class InvalidsParser extends RegexpParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Oracle ";
    /** Pattern of javac compiler warnings. */
    private static final String INVALIDS_PATTERN = "\\s*(\\w+),([a-zA-Z#_0-9/]*),([A-Z_ ]*),(.*),(\\d+),\\d+,([^:]*):\\s*(.*)";

    /**
     * Creates a new instance of <code>InvalidsParser</code>.
     */
    public InvalidsParser() {
        super(INVALIDS_PATTERN);
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String type = WARNING_TYPE + StringUtils.capitalize(StringUtils.lowerCase(matcher.group(4)));
        Warning warning = new Warning(matcher.group(2) + "." + matcher.group(3), getLineNumber(matcher.group(5)), type, matcher.group(6), matcher.group(7));
        warning.setPackageName(matcher.group(1));

        return warning;
    }
}

