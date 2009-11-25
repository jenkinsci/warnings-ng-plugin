package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for the MSBuild/PcLint compiler warnings.
 *
 * @author Ulli Hafner
 */
public class MsBuildParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "MSBuild";
    /** Pattern of MSBuild compiler warnings. */
    private static final String MS_BUILD_WARNING_PATTERN = "(?:(.*)\\((\\d*).*\\)|.*LINK)\\s*:\\s*([Nn]ote|[Ii]nfo|[Ww]arning|(?:fatal\\s*)?[Ee]rror)\\s*([^:]*):\\s*(.*)|(.*)\\s*:.*error\\s*(LNK[0-9]+):\\s*(.*)";

    /**
     * Creates a new instance of <code>MsBuildParser</code>.
     */
    public MsBuildParser() {
        super(MS_BUILD_WARNING_PATTERN, WARNING_TYPE);
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = determineFileName(matcher);
        if (StringUtils.isNotBlank(matcher.group(6))) {
            return new Warning(fileName, 0, getName(), matcher.group(7), matcher.group(8), Priority.HIGH);
        }
        else {
            return new Warning(fileName, getLineNumber(matcher.group(2)), getName(),
                    matcher.group(4), matcher.group(5), determinePriority(matcher));
        }
    }

    /**
     * Determines the name of the file that is cause of the warning.
     *
     * @param matcher
     *            the matcher to get the matches from
     * @return the name of the file with a warning
     */
    private String determineFileName(final Matcher matcher) {
        String fileName;
        if (StringUtils.isNotBlank(matcher.group(6))) {
            fileName = matcher.group(6);
        }
        else {
            fileName = matcher.group(1);
        }
        if (StringUtils.isBlank(fileName)) {
            fileName = StringUtils.substringBetween(matcher.group(5), "'");
        }
        if (StringUtils.isBlank(fileName)) {
            fileName = "unknown.file";
        }
        return fileName;
    }

    /**
     * Determines the priority of the warning.
     *
     * @param matcher
     *            the matcher to get the matches from
     * @return the priority of the warning
     */
    private Priority determinePriority(final Matcher matcher) {
        if (isOfType(matcher, "note") || isOfType(matcher, "info")) {
            return Priority.LOW;
        }
        else if (isOfType(matcher, "warning")) {
            return Priority.NORMAL;
        }
        return Priority.HIGH;
    }

    /**
     * Returns whether the warning type is of the specified type.
     *
     * @param matcher
     *            the matcher
     * @param type
     *            the type to match with
     * @return <code>true</code> if the warning type is of the specified type
     */
    private boolean isOfType(final Matcher matcher, final String type) {
        return StringUtils.containsIgnoreCase(matcher.group(3), type);
    }
}

