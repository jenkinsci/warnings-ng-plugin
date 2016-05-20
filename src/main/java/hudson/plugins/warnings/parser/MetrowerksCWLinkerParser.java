package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for Metrowerks Codewarrior 4.x linker warnings.
 *
 * @author Sven Lübke
 */
@Extension
public class MetrowerksCWLinkerParser extends RegexpLineParser {
    /** Pattern of MW CodeWarrior linker warnings. */
    private static final String CW_LINKER_WARNING_PATTERN = "^(INFORMATION|WARNING|ERROR) (.+)$";

    /**
     * Creates a new instance of <code>MetrowerksCWLinkerParser</code>.
     */
    public MetrowerksCWLinkerParser() {
        super(Messages._Warnings_MetrowerksCWLinker_ParserName(),
                Messages._Warnings_MetrowerksCWLinker_LinkName(),
                Messages._Warnings_MetrowerksCWLinker_TrendName(),
                CW_LINKER_WARNING_PATTERN);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        /* String fileName = matcher.group(3); */
        String message = matcher.group(2);
        String message_category = matcher.group(1);
        Priority priority;

        String category;
        if ("error".equalsIgnoreCase(message_category)) {
            priority = Priority.HIGH;
            category = "ERROR";
        }
        else if ("information".equalsIgnoreCase(message_category)) {
            priority = Priority.LOW;
            category = "Info";
        }
        else {
            priority = Priority.NORMAL;
            category = "Warning";
        }
        return createWarning("See Warning message", 0, category, message, priority);
    }
}

