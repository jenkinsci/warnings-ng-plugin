package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for gcc 4.x compiler warnings.
 *
 * @author Frederic Chateau
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class Gcc4CompilerParser extends RegexpLineParser {
    private static final long serialVersionUID = 5490211629355204910L;
    private static final String ERROR = "error";
    private static final String GCC_WARNING_PATTERN = ANT_TASK + "(.+?):(\\d+):(?:(\\d+):)? (warning|.*error): (.*)$";
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\[-W(.+)\\]$");

    /**
     * Creates a new instance of <code>Gcc4CompilerParser</code>.
     */
    public Gcc4CompilerParser() {
        super(Messages._Warnings_gcc4_ParserName(),
                Messages._Warnings_gcc4_LinkName(),
                Messages._Warnings_gcc4_TrendName(),
                GCC_WARNING_PATTERN);
    }

    @Override
    protected String getId() {
        return "GNU compiler 4 (gcc)";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(1);
        int lineNumber = getLineNumber(matcher.group(2));
        int column = getLineNumber(matcher.group(3));
        String message = matcher.group(5);
        Priority priority;

        StringBuilder category = new StringBuilder();
        if (matcher.group(4).contains(ERROR)) {
            priority = Priority.HIGH;
            category.append("Error");
        }
        else {
            priority = Priority.NORMAL;
            category.append("Warning");

            Matcher classMatcher = CLASS_PATTERN.matcher(message);
            if (classMatcher.find() && classMatcher.group(1) != null) {
                category.append(':').append(classMatcher.group(1));
            }
        }

        Warning warning = createWarning(fileName, lineNumber, category.toString(), message, priority);
        warning.setColumnPosition(column);
        return warning;
    }
}

