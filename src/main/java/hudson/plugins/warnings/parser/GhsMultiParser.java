package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the GHS Multi compiler warnings.
 *
 * @author Joseph Boulos
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class GhsMultiParser extends RegexpDocumentParser {
    private static final long serialVersionUID = 8149238560432255036L;
    private static final String GHS_MULTI_WARNING_PATTERN = "\\.(.*)\\,\\s*line\\s*(\\d+):\\s*(warning|error)\\s*([^:]+):\\s*(?m)([^\\^]*)\\s*\\^";

    /**
     * Creates a new instance of {@link GhsMultiParser}.
     */
    public GhsMultiParser() {
        super(Messages._Warnings_ghs_ParserName(),
                Messages._Warnings_ghs_LinkName(),
                Messages._Warnings_ghs_TrendName(),
                GHS_MULTI_WARNING_PATTERN, true);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(1);
        int lineNumber = getLineNumber(matcher.group(2));
        String type = StringUtils.capitalize(matcher.group(3));
        String category = matcher.group(4);
        String message = matcher.group(5);
        Priority priority;
        if ("warning".equalsIgnoreCase(type)) {
            priority = Priority.NORMAL;
        }
        else {
            priority = Priority.HIGH;
        }
        return createWarning(fileName, lineNumber, category, message, priority);
    }
}

