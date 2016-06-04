package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for armcc5 compiler warnings.
 *
 * @author Dmytro Kutianskyi
 */
@Extension
public class Armcc5CompilerParser extends RegexpLineParser {
    private static final long serialVersionUID = -2677728927938443701L;

    private static final String ARMCC5_WARNING_PATTERN = "^(.+)\\((\\d+)\\): (warning|error):  #(.+): (.+)$";
    
    /**
     * Creates a new instance of {@link Armcc5CompilerParser}.
     */
    public Armcc5CompilerParser() {
        super(Messages._Warnings_Armcc_ParserName(),
                Messages._Warnings_Armcc_LinkName(),
                Messages._Warnings_Armcc_TrendName(),
            ARMCC5_WARNING_PATTERN, true);
    }
    
    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("#");
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(1);
        int lineNumber = getLineNumber(matcher.group(2));
        String type = matcher.group(3);
        String errorCode = matcher.group(4);
        String message = matcher.group(5);
        Priority priority;

        if ("error".equalsIgnoreCase(type)) {
            priority = Priority.HIGH;
        }
        else {
            priority = Priority.NORMAL;
        }

        return createWarning(fileName, lineNumber, errorCode + " - " + message, priority);
    }
}

