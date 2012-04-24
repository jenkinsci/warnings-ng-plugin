package hudson.plugins.warnings.parser;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the Diab C++ compiler warnings.
 *
 * @author Ulli Hafner
 */
@Extension
public class DiabCParser extends RegexpLineParser {
    private static final long serialVersionUID = -1251248150596418456L;

    private static final String DIAB_CPP_WARNING_PATTERN = "^\\s*\"(.*)\"\\s*,\\s*line\\s*(\\d+)\\s*:\\s*(warning|error)\\s*\\(dcc:(\\d+)\\)\\s*:\\s*(.*)$";

    /**
     * Creates a new instance of <code>HpiCompileParser</code>.
     */
    public DiabCParser() {
        super(Messages._Warnings_diabc_ParserName(),
	      Messages._Warnings_diabc_LinkName(),
	      Messages._Warnings_diabc_TrendName(),
	      DIAB_CPP_WARNING_PATTERN);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        if ("warning".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.NORMAL;
        }
        else {
            priority = Priority.HIGH;
        }
        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), matcher.group(4), matcher.group(5), priority);
    }
}

