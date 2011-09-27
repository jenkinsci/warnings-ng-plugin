package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for the YUI Compressor warnings.
 */
public class YuiCompressorParser extends RegexpDocumentParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "YUI Compressor";
    /** Pattern of the YUI Compressor 2-lines warnings. */
    private static final String YUI_COMPRESSOR_WARNING_PATTERN = "\\[WARNING\\] ([^:]+):line ([^:]+):column ([^:]+):(.*)\\r?\\n^(.*)$";

    private static final Pattern UNUSED_SYMBOL_PATTERN = Pattern.compile(
            "The symbol [^ ]+ is declared but is apparently never used.*");
    private static final Pattern UNUSED_VARIABLE_PATTERN = Pattern.compile(
            "The variable [^ ]+ has already been declared in the same scope.*");

    /**
     * Creates a new instance of <code>YuiCompressorParser</code>.
     */
    public YuiCompressorParser() {
        super(YUI_COMPRESSOR_WARNING_PATTERN, true, WARNING_TYPE);
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
        final String messageHeader = matcher.group(4);
        CategoryAndPriority categoryAndPriority = getCategoryAndPriority(messageHeader);
        final String messageDetails = matcher.group(5);
        final String message = messageHeader + " [" + messageDetails + "]";
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE,
                categoryAndPriority.getCategory(), message, categoryAndPriority.getPriority());
    }

    private CategoryAndPriority getCategoryAndPriority(final String message) {
        if (message.startsWith("Found an undeclared symbol")) {
            return CategoryAndPriority.UNDECLARED_SYMBOL;
        }
        if (message.startsWith("Try to use a single 'var' statement per scope")) {
            return CategoryAndPriority.USE_SINGLE_VAR;
        }
        if (UNUSED_SYMBOL_PATTERN.matcher(message).matches()) {
            return CategoryAndPriority.UNUSED_SYMBOL;
        }
        if (UNUSED_VARIABLE_PATTERN.matcher(message).matches()) {
            return CategoryAndPriority.DUPLICATE_VAR;
        }
        return CategoryAndPriority.UNKNOWN;
    }

    /**
     * Handles category and priority of the warning.
     */
    private static enum CategoryAndPriority {
        UNDECLARED_SYMBOL("Undeclared symbol"), USE_SINGLE_VAR("Use single 'var' per scope",
                Priority.LOW), UNUSED_SYMBOL("Unused symbol"), DUPLICATE_VAR("Duplicate variable",
                Priority.HIGH), UNKNOWN("");

        private final String category;
        private final Priority priority;

        private CategoryAndPriority(final String category) {
            this(category, Priority.NORMAL);
        }

        private CategoryAndPriority(final String category, final Priority priority) {
            this.category = category;
            this.priority = priority;
        }

        public String getCategory() {
            return category;
        }

        public Priority getPriority() {
            return priority;
        }
    }
}
