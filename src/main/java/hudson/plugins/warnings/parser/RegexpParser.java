package hudson.plugins.warnings.parser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.Localizable;

import hudson.plugins.analysis.util.PackageDetectors;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Parses an input stream for compiler warnings using the provided regular expression.
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("deprecation")
public abstract class RegexpParser extends AbstractWarningsParser implements WarningsParser {
    private static final long serialVersionUID = -82635675595933170L;

    /** Used to define a false positive warnings that should be excluded after the regular expression scan. */
    protected static final Warning FALSE_POSITIVE = new Warning(StringUtils.EMPTY, 0, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
    /** Warning classification. */
    protected static final String DEPRECATION = "Deprecation";
    /** Warning classification. */
    protected static final String PROPRIETARY_API = "Proprietary API";
    /** Pattern identifying an ant task debug output prefix. */
    protected static final String ANT_TASK = "^(?:.*\\[.*\\])?\\s*";

    /** Pattern of compiler warnings. */
    private Pattern pattern;

    /** {@inheritDoc} */
    public String getName() {
        return getGroup();
    }

    private void setPattern(final String warningPattern, final boolean useMultiLine) {
        if (useMultiLine) {
            pattern = Pattern.compile(warningPattern, Pattern.MULTILINE);
        }
        else {
            pattern = Pattern.compile(warningPattern);
        }
    }

    /**
     * Creates a new instance of {@link RegexpParser}.
     * @param parserName
     *            name of the parser
     * @param linkName
     *            name of the project action link
     * @param trendName
     *            name of the trend graph
     * @param warningPattern
     *            pattern of compiler warnings.
     * @param useMultiLine
     *            Enables multi line mode. In multi line mode the expressions
     *            <tt>^</tt> and <tt>$</tt> match just after or just before,
     *            respectively, a line terminator or the end of the input
     *            sequence. By default these expressions only match at the
     *            beginning and the end of the entire input sequence.
     */
    public RegexpParser(final Localizable parserName, final Localizable linkName, final Localizable trendName,
            final String warningPattern, final boolean useMultiLine) {
        super(parserName, linkName, trendName);

        setPattern(warningPattern, useMultiLine);
    }

    /**
     * Parses the specified string content and creates annotations for each
     * found warning.
     *
     * @param content
     *            the content to scan
     * @param warnings
     *            the found annotations
     * @throws ParsingCanceledException
     *             indicates that the user canceled the operation
     */
    protected void findAnnotations(final String content, final List<FileAnnotation> warnings) throws ParsingCanceledException {
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            Warning warning = createWarning(matcher);
            if (warning != FALSE_POSITIVE) { // NOPMD
                detectPackageName(warning);
                warnings.add(warning);
            }
            if (Thread.interrupted()) {
                throw new ParsingCanceledException();
            }
        }
    }

    /**
     * Detects the package name for the specified warning.
     *
     * @param warning the warning
     */
    private void detectPackageName(final Warning warning) {
        if (!warning.hasPackageName()) {
            warning.setPackageName(PackageDetectors.detectPackageName(warning.getFileName()));
        }
    }

    /**
     * Creates a new annotation for the specified pattern. This method is called
     * for each matching line in the specified file. If a match is a false
     * positive, then you can return the constant {@link #FALSE_POSITIVE} to
     * ignore this warning.
     *
     * @param matcher
     *            the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    protected abstract Warning createWarning(final Matcher matcher);

    /**
     * Classifies the warning message: tries to guess a category from the
     * warning message.
     *
     * @param message
     *            the message to check
     * @return warning category, empty string if unknown
     */
    protected String classifyWarning(final String message) {
        if (StringUtils.contains(message, "proprietary")) {
            return PROPRIETARY_API;
        }
        if (StringUtils.contains(message, "deprecated")) {
            return DEPRECATION;
        }
        return StringUtils.EMPTY;
    }

    /**
     * Returns a category for the current warning. If the provided category is
     * not empty, then a capitalized string is returned. Otherwise the category
     * is obtained from the specified message text.
     * @param group
     *            the warning category (might be empty)
     * @param message
     *            the warning message
     *
     * @return the actual category
     */
    protected String classifyIfEmpty(final String group, final String message) {
        String category = StringUtils.capitalize(group);
        if (StringUtils.isEmpty(category)) {
            category = classifyWarning(message);
        }
        return category;
    }

    /**
     * Creates a new instance of {@link RegexpParser}. Uses a single line matcher.
     *
     * @param warningPattern
     *            pattern of compiler warnings.
     * @param name
     *            name of the parser
     * @deprecated use
     *             {@link #RegexpParser(Localizable, Localizable, Localizable, String, boolean)}
     */
    @Deprecated
    public RegexpParser(final String warningPattern, final String name) {
        this(warningPattern, false, name);
    }

    /**
     * Creates a new instance of {@link RegexpParser}.
     *
     * @param warningPattern
     *            pattern of compiler warnings.
     * @param useMultiLine
     *            Enables multi line mode. In multi line mode the expressions
     *            <tt>^</tt> and <tt>$</tt> match just after or just before,
     *            respectively, a line terminator or the end of the input
     *            sequence. By default these expressions only match at the
     *            beginning and the end of the entire input sequence.
     * @param name
     *            name of the parser
     * @deprecated use
     *             {@link #RegexpParser(Localizable, Localizable, Localizable, String, boolean)}
     */
    @Deprecated
    public RegexpParser(final String warningPattern, final boolean useMultiLine, final String name) {
        super(name);

        setPattern(warningPattern, useMultiLine);
    }
}
