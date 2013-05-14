package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the YUI Compressor warnings.
 */
@Extension
public class YuiCompressorParser extends RegexpDocumentParser {
    private static final long serialVersionUID = -4807932429496693096L;
    private static final String YUI_COMPRESSOR_WARNING_PATTERN = "\\[WARNING\\] (.*)\\r?\\n^(.*)$";

    private static final Pattern UNUSED_SYMBOL_PATTERN = Pattern.compile(
            "The symbol [^ ]+ is declared but is apparently never used.*");
    private static final Pattern UNUSED_VARIABLE_PATTERN = Pattern.compile(
            "The variable [^ ]+ has already been declared in the same scope.*");
    private static final Pattern UNUSED_FUNCTION_PATTERN = Pattern.compile(
            "The function [^ ]+ has already been declared in the same scope.*");
    private static final Pattern INVALID_HINT_PATTERN = Pattern.compile(
            "Invalid hint syntax: [^ ]+");
    private static final Pattern UNSUPPORTED_HINT_PATTERN = Pattern.compile(
            "Unsupported hint value: [^ ]+");
    private static final Pattern UNKNOWN_HINT_PATTERN = Pattern.compile(
            "Hint refers to an unknown identifier: [^ ]+");
    private static final Pattern PRINT_SYMBOL_PATTERN = Pattern.compile(
            "This symbol cannot be printed: [^ ]+");

    /**
     * Creates a new instance of <code>YuiCompressorParser</code>.
     */
    public YuiCompressorParser() {
        super(Messages._Warnings_YUICompressor_ParserName(),
                Messages._Warnings_YUICompressor_LinkName(),
                Messages._Warnings_YUICompressor_TrendName(),
                YUI_COMPRESSOR_WARNING_PATTERN, true);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        final String messageHeader = matcher.group(1);
        CategoryAndPriority categoryAndPriority = getCategoryAndPriority(messageHeader);
        final String messageDetails = matcher.group(2);
        final String message = messageHeader + " [" + messageDetails + "]";
        return createWarning("unknown.file", 0,
                categoryAndPriority.getCategory(), message, categoryAndPriority.getPriority());
    }

    // CHECKSTYLE:OFF
    private CategoryAndPriority getCategoryAndPriority(final String message) { // NOPMD
        if (message.startsWith("Found an undeclared symbol")) {
            return CategoryAndPriority.UNDECLARED_SYMBOL;
        }
        if (message.startsWith("Try to use a single 'var' statement per scope")) {
            return CategoryAndPriority.USE_SINGLE_VAR;
        }
        if (message.startsWith("Using JScript conditional comments is not recommended")) {
            return CategoryAndPriority.USE_JSCRIPT;
        }
        if (message.startsWith("Using 'eval' is not recommended")) {
            return CategoryAndPriority.USE_EVAL;
        }
        if (message.startsWith("Using 'with' is not recommended")) {
            return CategoryAndPriority.USE_WITH;
        }
        if (UNUSED_SYMBOL_PATTERN.matcher(message).matches()) {
            return CategoryAndPriority.UNUSED_SYMBOL;
        }
        if (UNUSED_VARIABLE_PATTERN.matcher(message).matches()) {
            return CategoryAndPriority.DUPLICATE_VAR;
        }
        if (UNUSED_FUNCTION_PATTERN.matcher(message).matches()) {
            return CategoryAndPriority.DUPLICATE_FUN;
        }
        if (INVALID_HINT_PATTERN.matcher(message).matches()) {
            return CategoryAndPriority.INVALID_HINT;
        }
        if (UNSUPPORTED_HINT_PATTERN.matcher(message).matches()) {
            return CategoryAndPriority.UNSUPPORTED_HINT;
        }
        if (UNKNOWN_HINT_PATTERN.matcher(message).matches()) {
            return CategoryAndPriority.UNKNOWN_HINT;
        }
        if (PRINT_SYMBOL_PATTERN.matcher(message).matches()) {
            return CategoryAndPriority.PRINT_SYMBOL;
        }
        return CategoryAndPriority.UNKNOWN;
    }
    // CHECKSTYLE:ON

    /**
     * Handles category and priority of the warning.
     */
    private static enum CategoryAndPriority {
        UNDECLARED_SYMBOL("Undeclared symbol"),
        USE_SINGLE_VAR("Use single 'var' per scope", Priority.LOW),
        UNUSED_SYMBOL("Unused symbol"),
        DUPLICATE_VAR("Duplicate variable", Priority.HIGH),
        UNKNOWN(""),
        DUPLICATE_FUN("Duplicate function", Priority.HIGH),
        INVALID_HINT("Invalid hint"),
        UNSUPPORTED_HINT("Unsupported hint", Priority.LOW),
        UNKNOWN_HINT("Unknown hint", Priority.LOW),
        USE_JSCRIPT("Use Jscript", Priority.HIGH),
        USE_EVAL("Use eval", Priority.HIGH),
        USE_WITH("Use with", Priority.HIGH),
        PRINT_SYMBOL("Cannot print symbol", Priority.LOW);

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
