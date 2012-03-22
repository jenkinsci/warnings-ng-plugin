package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for Eclipse compiler warnings.
 *
 * @author Ulli Hafner
 */
public class EclipseParser extends RegexpDocumentParser {
    private static final long serialVersionUID = 425883472788422955L;
    private static final String ANT_ECLIPSE_WARNING_PATTERN = "(WARNING|ERROR)\\s*in\\s*(.*)\\(at line\\s*(\\d+)\\).*(?:\\r?\\n[^\\^]*)+(?:\\r?\\n(.*)([\\^]+).*)\\r?\\n(?:\\s*\\[.*\\]\\s*)?(.*)";

    /**
     * Creates a new instance of {@link EclipseParser}.
     */
    public EclipseParser() {
        super(Messages._Warnings_EclipseParser_ParserName(),
                Messages._Warnings_EclipseParser_LinkName(),
                Messages._Warnings_EclipseParser_TrendName(),
                ANT_ECLIPSE_WARNING_PATTERN, true);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String type = StringUtils.capitalize(matcher.group(1));
        Priority priority;
        if ("warning".equalsIgnoreCase(type)) {
            priority = Priority.NORMAL;
        }
        else {
            priority = Priority.HIGH;
        }
        Warning warning = createWarning(matcher.group(2), getLineNumber(matcher.group(3)), matcher.group(6), priority);

        int columnStart = StringUtils.defaultString(matcher.group(4)).length();
        int columnEnd = columnStart + matcher.group(5).length();
        warning.setColumnPosition(columnStart, columnEnd);

        return warning;
    }
}

