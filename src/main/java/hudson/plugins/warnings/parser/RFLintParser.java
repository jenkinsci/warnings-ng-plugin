package hudson.plugins.warnings.parser;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * Created by traitanit on 3/27/2017 AD.
 */
@Extension
public class RFLintParser extends RegexpLineParser {

    private static final String RFLINT_ERROR_PATTERN = "(.*):(\\d+) \\[([WEI])\\]: (.*) \\((\\d+)\\) \\((.*)\\)";

    public RFLintParser(){
        super(Messages._Warnings_RFLint_ParserName(),
                Messages._Warnings_RFLint_LinkName(),
                Messages._Warnings_RFLint_TrendName(),
                RFLINT_ERROR_PATTERN, true);
    }

    @Override
    protected boolean isLineInteresting(String line) {
        return line.contains("[");
    }

    @Override
    protected Warning createWarning(Matcher matcher) {
        String message = matcher.group(4);
        String category = classifyIfEmpty(matcher.group(3), message);
        Priority priority = Priority.LOW;
        switch (category.charAt(0)){
            case 'E':
                priority = Priority.HIGH;
                break;
            case 'W':
                priority = Priority.NORMAL;
                break;
            case 'I':
                priority = Priority.LOW;
                break;
        }
        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), category, message, priority);
    }
}
