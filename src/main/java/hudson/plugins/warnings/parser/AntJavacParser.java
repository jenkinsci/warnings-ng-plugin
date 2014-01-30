package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import hudson.Extension;

/**
 * A parser for the ant javac compiler warnings.
 *
 * @author Ulli Hafner
 */
@Extension
public class AntJavacParser extends RegexpLineParser {
    private static final long serialVersionUID = 1737791073711198075L;

    /** Pattern of javac compiler warnings. */
    private static final String ANT_JAVAC_WARNING_PATTERN = ANT_TASK
            + "\\s*(.*java):(\\d*):\\s*(?:warning|\u8b66\u544a)\\s*:\\s*(?:\\[(\\w*)\\])?\\s*(.*)$"
            + "|^\\s*\\[.*\\]\\s*warning.*\\]\\s*(.*\"(.*)\".*)$"
            + "|^(.*class)\\s*:\\s*warning\\s*:\\s*(.*)$";
    // \u8b66\u544a is Japanese l10n

    /**
     * Creates a new instance of {@link AntJavacParser}.
     */
    public AntJavacParser() {
        super(Messages._Warnings_JavaParser_ParserName(),
                Messages._Warnings_JavaParser_LinkName(),
                Messages._Warnings_JavaParser_TrendName(),
                ANT_JAVAC_WARNING_PATTERN, true);
    }

    @Override
    public String getSmallImage() {
        return JavacParser.JAVA_SMALL_ICON;
    }

    @Override
    public String getLargeImage() {
        return JavacParser.JAVA_LARGE_ICON;
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("warning") || line.contains("\u8b66\u544a");
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        if (StringUtils.isNotBlank(matcher.group(7))) {
            return createWarning(matcher.group(7), 0, getGroup(), matcher.group(8));
        }
        else if (StringUtils.isBlank(matcher.group(5))) {
            String message = matcher.group(4);
            String category = classifyIfEmpty(matcher.group(3), message);
            return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), category, message);
        }
        else {
            return createWarning(matcher.group(6), 0, "Path", matcher.group(5));
        }
    }

    @Override
    protected String getId() {
        return "Java Compiler"; // old ID in serialization
    }

}

