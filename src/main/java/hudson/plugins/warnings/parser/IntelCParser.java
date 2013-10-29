package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for messages from the Intel C and Fortran compilers.
 *
 * @author Vangelis Livadiotis
 */
@Extension
public class IntelCParser extends RegexpLineParser {
    private static final long serialVersionUID = 8409744276858003050L;
    private static final String INTEL_PATTERN = "^(.*)\\((\\d*)\\)?:(?:\\s*\\(col\\. (\\d+)\\))?.*((?:remark|warning|error)\\s*#*\\d*)\\s*:\\s*(.*)$";

    /**
     * Creates a new instance of {@link IntelCParser}.
     */
    public IntelCParser() {
        super(Messages._Warnings_IntelC_ParserName(),
                Messages._Warnings_IntelC_LinkName(),
                Messages._Warnings_IntelC_TrendName(),
                INTEL_PATTERN, true);
    }

    @Override
    protected String getId() {
        return "Intel compiler";
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("warning")
                || line.contains("error")
                || line.contains("remark");
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


