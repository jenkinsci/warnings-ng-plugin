package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.JavaPackageDetector;
import hudson.plugins.warnings.util.model.FileAnnotation;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Parses an input stream for compiler warnings using the provided regular expression.
 *
 * @author Ulli Hafner
 */
public abstract class RegexpParser implements WarningsParser {
    /** Used to define a false positive warnings that should be excluded after the regular expression scan. */
    protected static final Warning FALSE_POSITIVE = new Warning(StringUtils.EMPTY, 0, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
    /** Warning classification. */
    protected static final String DEPRECATION = "Deprecation";
    /** Warning classification. */
    protected static final String PROPRIETARY_API = "Proprietary API";
    /** Pattern of compiler warnings. */
    private final Pattern pattern;
    /** Name of this parser. */
    private String name;

    /**
     * Creates a new instance of <code>RegexpParser</code>. Uses a single line matcher.
     *
     * @param warningPattern
     *            pattern of compiler warnings.
     * @param name
     *            name of the parser
     */
    public RegexpParser(final String warningPattern, final String name) {
        this(warningPattern, false, name);
    }

    /**
     * Creates a new instance of <code>RegexpParser</code>.
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
     */
    public RegexpParser(final String warningPattern, final boolean useMultiLine, final String name) {
        this.name = name;
        if (useMultiLine) {
            pattern = Pattern.compile(warningPattern, Pattern.MULTILINE);
        }
        else {
            pattern = Pattern.compile(warningPattern);
        }
    }

    /**
     * Parses the specified string content and creates annotations for each found warning.
     *
     * @param content the content to scan
     * @param warnings the found annotations
     */
    protected void findAnnotations(final String content, final List<FileAnnotation> warnings) {
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            Warning warning = createWarning(matcher);
            if (warning != FALSE_POSITIVE) {
                detectPackageName(warning);
                warnings.add(warning);
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
            String packageName = new JavaPackageDetector().detectPackageName(warning.getFileName());
            warning.setPackageName(packageName);
        }
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getName();
    }

    /** {@inheritDoc} */
    public String getName() {
        return name;
    }

    /**
     * Sets the name to the specified value.
     *
     * @param name the value to set
     */
    public void setName(final String name) {
        this.name = name;
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
     * Converts a string line number to an integer value. If the string is not a valid line number,
     * then 0 is returned which indicates a warning at the top of the file.
     *
     * @param lineNumber the line number (as a string)
     * @return the line number
     */
    protected final int getLineNumber(final String lineNumber) {
        if (StringUtils.isNotBlank(lineNumber)) {
            try {
                return Integer.parseInt(lineNumber);
            }
            catch (NumberFormatException exception) {
                // ignore and return 0
            }
        }
        return 0;
    }

    /**
     * Classifies the warning message: tries to guess a category from the warning message.
     *
     * @param message the message to check
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
}
