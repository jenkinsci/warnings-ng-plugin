package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;
import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.jvnet.localizer.Localizable;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the MSBuild/PcLint compiler warnings.
 *
 * @author Ulli Hafner
 */
@Extension
public class MsBuildParser extends RegexpLineParser {
    private static final long serialVersionUID = -2141974437420906595L;
    static final String WARNING_TYPE = "MSBuild";
    private static final String MS_BUILD_WARNING_PATTERN = ANT_TASK + "(?:\\s*\\d+>)?(?:(?:(?:(.*)\\((\\d*)(?:,(\\d+))?.*\\)|.*LINK)\\s*:|(.*):)\\s*([A-z-_]*\\s?(?:[Nn]ote|[Ii]nfo|[Ww]arning|(?:fatal\\s*)?[Ee]rror))\\s*:?\\s*([A-Za-z0-9]+)\\s*:\\s(?:\\s*([A-Za-z0-9.]+)\\s*:)?\\s*(.*?)(?: \\[([^\\]]*)[/\\\\][^\\]\\\\]+\\])?"
            + "|(.*)\\s*:.*error\\s*(LNK[0-9]+):\\s*(.*))$";

    /**
     * Creates a new instance of {@link MsBuildParser}.
     */
    public MsBuildParser() {
        this(Messages._Warnings_MSBuild_ParserName(),
                Messages._Warnings_MSBuild_LinkName(),
                Messages._Warnings_MSBuild_TrendName());
    }

    /**
     * Creates a new instance of {@link MsBuildParser}.
     *
     * @param parserName
     *            name of the parser
     * @param linkName
     *            name of the project action link
     * @param trendName
     *            name of the trend graph
     */
    public MsBuildParser(final Localizable parserName, final Localizable linkName, final Localizable trendName) {
        super(parserName, linkName, trendName, MS_BUILD_WARNING_PATTERN);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = determineFileName(matcher);
        if (StringUtils.isNotBlank(matcher.group(10))) {
            return createWarning(fileName, 0, matcher.group(11), matcher.group(12), Priority.HIGH);
        }
        else {
            Warning warning;
            if (StringUtils.isNotEmpty(matcher.group(7))) {
                warning = createWarning(fileName, getLineNumber(matcher.group(2)),
                        matcher.group(7), matcher.group(6), matcher.group(8), determinePriority(matcher));
            }
            else {
                String category = matcher.group(6);
                if ("Expected".matches(category)) {
                    return FALSE_POSITIVE;
                }
                warning = createWarning(fileName, getLineNumber(matcher.group(2)),
                        category, matcher.group(8), determinePriority(matcher));
            }
            warning.setColumnPosition(getLineNumber(matcher.group(3)));
            return warning;
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
        if (StringUtils.isNotBlank(matcher.group(4))) {
            fileName = matcher.group(4);
        }
        else if (StringUtils.isNotBlank(matcher.group(10))) {
            fileName = matcher.group(10);
        }
        else {
            fileName = matcher.group(1);
        }
        if (StringUtils.isBlank(fileName)) {
            fileName = StringUtils.substringBetween(matcher.group(8), "'");
        }
        if (StringUtils.isBlank(fileName)) {
            fileName = "unknown.file";
        }

        final String projectDir = matcher.group(9);
        if (StringUtils.isNotBlank(projectDir)
            && FilenameUtils.getPrefixLength(fileName) == 0
            && !fileName.trim().equals("MSBUILD")) {
            // resolve fileName relative to projectDir
            fileName = FilenameUtils.concat(projectDir, fileName);
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
        return StringUtils.containsIgnoreCase(matcher.group(5), type);
    }
}

