package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for TASKING VX compiler warnings.
 *
 * @author Sven Lübke
 */
@Extension
public class TaskingVXCompilerParser extends RegexpLineParser {
    /** Pattern of TASKING VX compiler warnings. */
    private static final String TASKING_VX_COMPILER_WARNING_PATTERN = "^.*? (I|W|E|F)(\\d+): (?:\\[\"(.*?)\" (\\d+)\\/(\\d+)\\] )?(.*)$";
                                                                      

    /**
     * Creates a new instance of <code>TaskingVXCompilerParser</code>.
     */
    public TaskingVXCompilerParser() {
        super(Messages._Warnings_TaskingVXCompiler_ParserName(),
                Messages._Warnings_TaskingVXCompiler_LinkName(),
                Messages._Warnings_TaskingVXCompiler_TrendName(),
                TASKING_VX_COMPILER_WARNING_PATTERN);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName;
        String msgType = matcher.group(1);
        int lineNumber;
        String message = matcher.group(6);
        Priority priority;
        String category;

        if(matcher.group(3) != null) {
          fileName = matcher.group(3);
        }
        else {
          fileName = "";
        }

        if(matcher.group(4) != null) {
          lineNumber = getLineNumber(matcher.group(4));
        }
        else {
          lineNumber = 0;
        }

        if ("E".equals(msgType)) {
            priority = Priority.HIGH;
            category = "ERROR";
        }
        else if ("F".equals(msgType)) {
            priority = Priority.HIGH;
            category = "License issue";
        }
        else if ("I".equals(msgType)) {
            priority = Priority.LOW;
            category = "Info";
        }
        else {
            priority = Priority.NORMAL;
            category = "Warning";
        }
        return createWarning(fileName, lineNumber, category, message, priority);
    }
}

