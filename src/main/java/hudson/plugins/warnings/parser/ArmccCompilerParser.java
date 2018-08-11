package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for armcc compiler warnings.
 *
 * @author Emanuele Zattin
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class ArmccCompilerParser extends RegexpLineParser {
    private static final long serialVersionUID = -2677728927938443703L;

    private static final String ARMCC_WARNING_PATTERN = "^\"(.+)\", line (\\d+): ([A-Z][a-z]+):\\D*(\\d+)\\D*?:\\s+(.+)$";

    /**
     * Creates a new instance of {@link ArmccCompilerParser}.
     */
    public ArmccCompilerParser() {
        super(Messages._Warnings_Armcc_ParserName(),
                Messages._Warnings_Armcc_LinkName(),
                Messages._Warnings_Armcc_TrendName(),
            ARMCC_WARNING_PATTERN);
    }

    @Override
    protected String getId() {
        return "Armcc";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(1);
        int lineNumber = getLineNumber(matcher.group(2));
        String type = matcher.group(3);
        int errorCode = getLineNumber(matcher.group(4));
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

