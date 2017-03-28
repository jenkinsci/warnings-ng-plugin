package hudson.plugins.warnings.parser;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * Created by traitanit on 3/27/2017 AD.
 */
@Extension
public class RFLintParser extends RegexpLineParser {

    private static final String RFLINT_ERROR_PATTERN = "(.*): ([W|E|I]): (\\d+), (\\d+): (.*) \\((.*)\\)";

    public RFLintParser(){
        super(Messages._Warnings_RFLint_ParserName(),
                Messages._Warnings_RFLint_LinkName(),
                Messages._Warnings_RFLint_TrendName(),
                RFLINT_ERROR_PATTERN, true);
    }

    @Override
    protected boolean isLineInteresting(String line) {
        return line.contains("W") || line.contains("E") || line.contains("I");
    }

    @Override
    protected Warning createWarning(Matcher matcher) {
        String message = matcher.group(5);
        String category = classifyIfEmpty(matcher.group(2), message);
        Priority priority = Priority.LOW;
        switch (category.charAt(0)){
            case 'E':
                priority = Priority.HIGH;
                category = "ERROR";
                break;
            case 'W':
                priority = Priority.NORMAL;
                category = "WARNING";
                break;
            case 'I':
                priority = Priority.LOW;
                category = "IGNORE";
                break;
        }
        return createWarning(matcher.group(1), getLineNumber(matcher.group(3)), category, message, priority);
    }
}
