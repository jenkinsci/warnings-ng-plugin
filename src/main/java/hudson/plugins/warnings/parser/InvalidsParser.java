package hudson.plugins.warnings.parser;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for Oracle Invalids.
 *
 * @author Ulli Hafner
 */
@Extension
public class InvalidsParser extends RegexpLineParser {
    private static final long serialVersionUID = 440910718005095427L;
    static final String WARNING_PREFIX = "Oracle ";
    private static final String INVALIDS_PATTERN = "^\\s*(\\w+),([a-zA-Z#_0-9/]*),([A-Z_ ]*),(.*),(\\d+),\\d+,([^:]*):\\s*(.*)$";

    /**
     * Creates a new instance of {@link InvalidsParser}.
     */
    public InvalidsParser() {
        super(Messages._Warnings_OracleInvalids_ParserName(),
                Messages._Warnings_OracleInvalids_LinkName(),
                Messages._Warnings_OracleInvalids_TrendName(),
                INVALIDS_PATTERN);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String type = WARNING_PREFIX + StringUtils.capitalize(StringUtils.lowerCase(matcher.group(4)));
        String category = matcher.group(6);
        Priority priority;
        if (StringUtils.contains(category, "PLW-07")) {
            priority = Priority.LOW;
        }
        else if (StringUtils.contains(category, "ORA")) {
            priority = Priority.HIGH;
        }
        else {
            priority = Priority.NORMAL;
        }
        Warning warning = new Warning(matcher.group(2) + "." + matcher.group(3), getLineNumber(matcher.group(5)), type, category, matcher.group(7), priority);
        warning.setPackageName(matcher.group(1));

        return warning;
    }
}

