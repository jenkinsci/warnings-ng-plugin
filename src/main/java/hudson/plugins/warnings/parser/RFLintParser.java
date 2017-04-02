package hudson.plugins.warnings.parser;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for <a href="http://robotframework.org/">Robot Framework</a>
 * Parse output from <a href="https://github.com/boakley/robotframework-lint">robotframework-lint</a>
 * To generate rflint file
 * cmd$ pip install robotframework-lint
 * cmd$ rflint path/to/test.robot
 * Created by traitanit on 3/27/2017 AD.
 */
@Extension
public class RFLintParser extends RegexpLineParser {

    private static final String RFLINT_ERROR_PATTERN = "([W|E|I]): (\\d+), (\\d+): (.*) \\((.*)\\)";
    private static final String RFLINT_FILE_PATTERN = "\\+\\s(.*)";
    private String fileName;

    public RFLintParser(){
        super(Messages._Warnings_RFLint_ParserName(),
                Messages._Warnings_RFLint_LinkName(),
                Messages._Warnings_RFLint_TrendName(),
                RFLINT_ERROR_PATTERN, true);
    }

    @Override
    protected boolean isLineInteresting(String line) {
        Pattern filePattern = Pattern.compile(RFLINT_FILE_PATTERN);
        Matcher matcher = filePattern.matcher(line);
        if (matcher.find()) {
            fileName = matcher.group(1);
            return false;
        }
        return Pattern.matches(RFLINT_ERROR_PATTERN, line);
    }


    @Override
    protected Warning createWarning(Matcher matcher) {
        String message = matcher.group(4);
        String category = classifyIfEmpty(matcher.group(1), message);
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
            default:
                break;
        }
        return createWarning(fileName, getLineNumber(matcher.group(2)), category, message, priority);
    }
}
