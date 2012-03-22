package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for the Inter C compiler warnings.
 *
 * @author Vangelis Livadiotis
 */
public class IntelCParser extends RegexpLineParser {
    private static final long serialVersionUID = 8409744276858003050L;
    private static final String INTEL_PATTERN = "^(.*)\\((\\d*)\\)?:.*((?:remark|warning|error)\\s*#*\\d*)\\s*:\\s*(.*)$";

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
        String category = StringUtils.capitalize(matcher.group(3));

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

        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), category, matcher.group(4), priority);
    }
}


