package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.Localizable;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for messages from the Intel C and Fortran compilers.
 *
 * @author Vangelis Livadiotis
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class IntelParser extends RegexpLineParser {
    private static final long serialVersionUID = 8409744276858003050L;
    private static final String INTEL_PATTERN = "^(.*)\\((\\d*)\\)?:(?:\\s*\\(col\\. (\\d+)\\))?.*((?:remark|warning|error)\\s*#*\\d*)\\s*:\\s*(.*)$";

    /**
     * Creates a new instance of {@link IntelParser}.
     */
    public IntelParser() {
        this(Messages._Warnings_IntelC_ParserName(), Messages._Warnings_IntelC_LinkName(),
                Messages._Warnings_IntelC_TrendName());
    }

    /**
     * Creates a new instance of {@link IntelParser} using the specified names.
     *
     * @param parserName
     *            name of the parser
     * @param linkName
     *            name of the project action link
     * @param trendName
     *            name of the trend graph
     */
    public IntelParser(final Localizable parserName, final Localizable linkName, final Localizable trendName) {
        super(parserName, linkName, trendName, INTEL_PATTERN, true);
    }

    @Override
    protected String getId() {
        return "Intel compiler";
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("warning") || line.contains("error") || line.contains("remark");
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String category = StringUtils.capitalize(matcher.group(4));

        Priority priority;
        if (StringUtils.startsWith(category, "Remark")) {
            priority = Priority.LOW;
        }
        else if (StringUtils.startsWith(category, "Error")) {
            priority = Priority.HIGH;
        }
        else {
            priority = Priority.NORMAL;
        }

        Warning warning = createWarning(matcher.group(1), getLineNumber(matcher.group(2)), category, matcher.group(5), priority);
        warning.setColumnPosition(getLineNumber(matcher.group(3)));
        return warning;
    }
}
