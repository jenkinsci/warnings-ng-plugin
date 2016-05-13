package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for Metrowerks Codewarrior 4.x compiler warnings.
 *
 * @author Sven Lübke
 */
@Extension
public class MetrowerksCWCompilerParser extends RegexpLineParser {
    /** Pattern of MW CodeWarrior compiler warnings. */
    private static final String CW_COMPILER_WARNING_PATTERN = "^(.+?)\\((\\d+)\\): (INFORMATION|WARNING|ERROR) (.+?): (.*)$";

    /**
     * Creates a new instance of <code>MetrowerksCWCompilerParser</code>.
     */
    public MetrowerksCWCompilerParser() {
        super(Messages._Warnings_MetrowerksCWCompiler_ParserName(),
                Messages._Warnings_MetrowerksCWCompiler_LinkName(),
                Messages._Warnings_MetrowerksCWCompiler_TrendName(),
                CW_COMPILER_WARNING_PATTERN);
    }

    @Override
    protected String getId() {
        return "Metrowerks Codewarrior Compiler";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(1);
        int lineNumber = getLineNumber(matcher.group(2));
        String message = matcher.group(5);
        Priority priority;

        StringBuilder category = new StringBuilder();
        if (matcher.group(3).equalsIgnoreCase("error")) {
            priority = Priority.HIGH;
            category.append("ERROR");
        }
        else if (matcher.group(3).equalsIgnoreCase("information")) {
            priority = Priority.LOW;			
            category.append("Info");
        }
		else {			
            priority = Priority.NORMAL;
            category.append("Warning");
        }
        Warning warning = createWarning(fileName, lineNumber, category.toString(), message, priority);
        return warning;
    }
}

