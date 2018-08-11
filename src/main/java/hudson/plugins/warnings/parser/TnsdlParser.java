package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the tnsdl translator warnings.
 *
 * @author Shaohua Wen
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class TnsdlParser extends RegexpLineParser {
    private static final long serialVersionUID = -7740789998865369930L;
    static final String WARNING_CATEGORY = "Error";
    private static final String TNSDL_WARNING_PATTERN = "^tnsdl((.*)?):\\(.*\\) (.*) \\((.*)\\):(.*)$";

    /**
     * Creates a new instance of {@link TnsdlParser}.
     */
    public TnsdlParser() {
        super(Messages._Warnings_TNSDL_ParserName(),
                Messages._Warnings_TNSDL_LinkName(),
                Messages._Warnings_TNSDL_TrendName(),
                TNSDL_WARNING_PATTERN, true);
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("tnsdl");
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(3);
        int lineNumber = getLineNumber(matcher.group(4));
        String message = matcher.group(5);
        Priority priority;

        if (matcher.group().contains("(E)")) {
            priority = Priority.HIGH;
        }
        else {
            priority = Priority.NORMAL;
        }

        return createWarning(fileName, lineNumber, WARNING_CATEGORY, message, priority);
    }
}

