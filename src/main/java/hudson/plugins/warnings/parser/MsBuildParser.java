package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.Localizable;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the MSBuild/PcLint compiler warnings.
 *
 * @author Ullrich Hafner
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class MsBuildParser extends RegexpLineParser {
    private static final long serialVersionUID = -2141974437420906595L;
    static final String WARNING_TYPE = "MSBuild";
    private static final String MS_BUILD_WARNING_PATTERN = "(?:^(?:.*)Command line warning ([A-Za-z0-9]+):\\s*(.*)\\s*\\[(.*)\\])|"
            + ANT_TASK + "(?:(?:\\s*\\d+>)?(?:(?:(?:(.*)\\((\\d*)(?:,(\\d+))?.*\\)|.*LINK)\\s*:|(.*):)\\s*([A-z-_]*\\s?(?:[Nn]ote|[Ii]nfo|[Ww]arning|(?:fatal\\s*)?[Ee]rror))\\s*:?\\s*([A-Za-z0-9]+)\\s*:\\s(?:\\s*([A-Za-z0-9.]+)\\s*:)?\\s*(.*?)(?: \\[([^\\]]*)[/\\\\][^\\]\\\\]+\\])?"
            + "|(.*)\\s*:.*error\\s*(LNK[0-9]+):\\s*(.*)))$";

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
        if (StringUtils.isNotBlank(matcher.group(2))) {
            return createWarning(fileName, 0, matcher.group(1), matcher.group(2), Priority.NORMAL);
        }
        else if (StringUtils.isNotBlank(matcher.group(13))) {
            return createWarning(fileName, 0, matcher.group(14), matcher.group(15), Priority.HIGH);
        }
        else {
            Warning warning;
            if (StringUtils.isNotEmpty(matcher.group(10))) {
                warning = createWarning(fileName, getLineNumber(matcher.group(5)),
                        matcher.group(10), matcher.group(9), matcher.group(11), determinePriority(matcher));
            }
            else {
                String category = matcher.group(9);
                if ("Expected".matches(category)) {
                    return FALSE_POSITIVE;
                }
                warning = createWarning(fileName, getLineNumber(matcher.group(5)),
                        category, matcher.group(11), determinePriority(matcher));
            }
            warning.setColumnPosition(getLineNumber(matcher.group(6)));
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
        if (StringUtils.isNotBlank(matcher.group(3))) {
            fileName = matcher.group(3);
        }
        else if (StringUtils.isNotBlank(matcher.group(7))) {
            fileName = matcher.group(7);
        }
        else if (StringUtils.isNotBlank(matcher.group(13))) {
            fileName = matcher.group(13);
        }
        else {
            fileName = matcher.group(4);
        }
        if (StringUtils.isBlank(fileName)) {
            fileName = StringUtils.substringBetween(matcher.group(11), "'");
        }
        if (StringUtils.isBlank(fileName)) {
            fileName = "unknown.file";
        }

        final String projectDir = matcher.group(12);
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
        return StringUtils.containsIgnoreCase(matcher.group(8), type);
    }
}

