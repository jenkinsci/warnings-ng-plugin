package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for Robocopy.
 *
 * @author Adrián Deccico
 *
 * captured samples:
 *    *EXTRA File                  0        b           Unknown Task
 *   100%        New File                   0        a.log
 *                 same                 0        a.log
 */
public class RobocopyParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Robocopy (please use /V in your commands!)";
    /** Pattern of perforce compiler warnings. */
    private static final String ROBOCOPY_WARNING_PATTERN = "^(.*)(EXTRA File|New File|same)\\s*(\\d*)\\s*(.*)$";

    /**
     * Creates a new instance of {@link RobocopyParser}.
     */
    public RobocopyParser() {
        super(ROBOCOPY_WARNING_PATTERN, WARNING_TYPE, true);
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
        String file = matcher.group(4).split("\\s{11}")[0];
        String message = file;
        String category = matcher.group(2);
        return new Warning(file, 0, WARNING_TYPE, category, message, Priority.NORMAL);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("        ");
    }
}

