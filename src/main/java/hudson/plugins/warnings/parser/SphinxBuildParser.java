package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the Sphinx build warnings.
 *
 * @author Robert Williams
 */
@Extension
public class SphinxBuildParser extends RegexpLineParser {
    private static final long serialVersionUID = 1L;
    private static final String SPHINX_BUILD_WARNING_PATTERN = "^(.*):(\\d+|None|): (.*?): (.*)";

    /**
     * Creates a new instance of {@link SphinxBuildParser}.
     */
    public SphinxBuildParser() {
        super(Messages._Warnings_SphinxBuild_ParserName(),
                Messages._Warnings_SphinxBuild_LinkName(),
                Messages._Warnings_SphinxBuild_TrendName(),
                SPHINX_BUILD_WARNING_PATTERN);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(4);
        String category = classifyIfEmpty(matcher.group(3), message);
        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), category, message, mapPriority(category));
    }

    private Priority mapPriority(final String priority) {
        if ("error".equalsIgnoreCase(priority)){
            return Priority.HIGH;
        }
        else { 
            return Priority.NORMAL;
        }
    }
}

