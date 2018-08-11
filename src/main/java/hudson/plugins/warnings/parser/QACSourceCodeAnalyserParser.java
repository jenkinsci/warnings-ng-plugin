package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for PRQA QA-C Sourcecode Analyser warnings.
 *
 * @author Sven Lübke
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class QACSourceCodeAnalyserParser extends RegexpLineParser {
    /** Pattern of QA-C Sourcecode Analyser warnings. */
    private static final String QAC_WARNING_PATTERN = "^(.+?)\\((\\d+),(\\d+)\\): (Err|Msg)\\((\\d+):(\\d+)\\) (.+?)$";

    /**
     * Creates a new instance of <code>QACSourceCodeAnalyserParser</code>.
     */
    public QACSourceCodeAnalyserParser() {
        super(Messages._Warnings_QAC_ParserName(),
                Messages._Warnings_QAC_LinkName(),
                Messages._Warnings_QAC_TrendName(),
                QAC_WARNING_PATTERN);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(1);
        int lineNumber = getLineNumber(matcher.group(2));
        String message = matcher.group(7);
        Priority priority;

        String category;
        if ("err".equalsIgnoreCase(matcher.group(4))) {
            priority = Priority.HIGH;
            category = "ERROR";
        }
        else {
            priority = Priority.NORMAL;
            category = "Warning";
        }
        return createWarning(fileName, lineNumber, category, message, priority);
    }
}

