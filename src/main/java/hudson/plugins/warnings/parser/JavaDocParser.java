package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the ant JavaDoc compiler warnings.
 *
 * @author Ulli Hafner
 */
@Extension
public class JavaDocParser extends RegexpLineParser {
    private static final long serialVersionUID = 7127568148333474921L;
    private static final String JAVA_DOC_WARNING_PATTERN = "(?:\\s*\\[(?:javadoc|WARNING)\\]\\s*)?(?:(?:(.*):(\\d+))|(?:\\s*javadoc\\s*)):\\s*(warning|error)\\s*[-:]\\s*(.*)";

    /**
     * Creates a new instance of {@link JavaDocParser}.
     */
    public JavaDocParser() {
        super(Messages._Warnings_JavaDoc_ParserName(),
                Messages._Warnings_JavaDoc_LinkName(),
                Messages._Warnings_JavaDoc_TrendName(),
                JAVA_DOC_WARNING_PATTERN);
    }

    @Override
    protected String getId() {
        return "JavaDoc";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(4);
        String type = matcher.group(3);
        Priority priority;
        if ("warning".equals(type)) {
            priority = Priority.NORMAL;
        }
        else {
            priority = Priority.HIGH;
        }
        String fileName = StringUtils.defaultIfEmpty(matcher.group(1), " - ");

        return createWarning(fileName, getLineNumber(matcher.group(2)), message, priority);
    }
}

