package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for Robocopy.
 *
 * @author Adrian Deccico
 *
 * captured samples:
 *    *EXTRA File                  0        b           Unknown Task
 *   100%        New File                   0        a.log
 *                 same                 0        a.log
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class RobocopyParser extends RegexpLineParser {
    private static final long serialVersionUID = -671744745118772873L;
    /** Pattern of perforce compiler warnings. */
    private static final String ROBOCOPY_WARNING_PATTERN = "^(.*)(EXTRA File|New File|same)\\s*(\\d*)\\s*(.*)$";

    /**
     * Creates a new instance of {@link RobocopyParser}.
     */
    public RobocopyParser() {
        super(Messages._Warnings_Robocopy_ParserName(),
                Messages._Warnings_Robocopy_LinkName(),
                Messages._Warnings_Robocopy_TrendName(),
                ROBOCOPY_WARNING_PATTERN, true);
    }

    @Override
    protected String getId() {
        return "Robocopy (please use /V in your commands!)";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String file = matcher.group(4).split("\\s{11}")[0];
        String message = file;
        String category = matcher.group(2);
        return createWarning(file, 0, category, message, Priority.NORMAL);
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("        ");
    }
}

