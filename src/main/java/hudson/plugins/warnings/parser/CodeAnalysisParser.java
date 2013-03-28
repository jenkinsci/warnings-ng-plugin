package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.Localizable;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the CodeAnalysis compiler warnings.
 *
 * @author Rafal Jasica
 */
@Extension
public class CodeAnalysisParser extends RegexpLineParser {
    private static final long serialVersionUID = -125874563249851L;
    private static final String WARNING_PATTERN = ANT_TASK
            + "((MSBUILD)|((.+)\\((\\d+)\\)))\\s*:\\s*[Ww]arning\\s*:\\s*(\\w*)\\s*:\\s*(Microsoft\\.|)(\\w*(\\.\\w*)*)\\s*:\\s*(.*)\\[(.*)\\]\\s*$";

    /**
     * Creates a new instance of {@link CodeAnalysisParser}.
     */
    public CodeAnalysisParser() {
        this(Messages._Warnings_CodeAnalysis_ParserName(), Messages._Warnings_CodeAnalysis_LinkName(),
                Messages._Warnings_CodeAnalysis_TrendName());
    }

    /**
     * Creates a new instance of {@link CodeAnalysisParser}.
     *
     * @param parserName
     *            name of the parser
     * @param linkName
     *            name of the project action link
     * @param trendName
     *            name of the trend graph
     */
    public CodeAnalysisParser(final Localizable parserName, final Localizable linkName, final Localizable trendName) {
        super(parserName, linkName, trendName, WARNING_PATTERN);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        if (StringUtils.isNotBlank(matcher.group(2))) {
            return createWarning(matcher.group(11), 0, matcher.group(6), matcher.group(8), matcher.group(10), Priority.NORMAL);
        }
        else {
            return createWarning(matcher.group(4), getLineNumber(matcher.group(5)), matcher.group(6), matcher.group(8), matcher.group(10), Priority.NORMAL);
        }
    }
}
