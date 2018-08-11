package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import hudson.Extension;
import hudson.console.ConsoleNote;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for <a href="http://robotframework.org/">Robot Framework</a>
 * Parse output from <a href="https://github.com/boakley/robotframework-lint">robotframework-lint</a>
 * To generate rflint file
 * cmd$ pip install robotframework-lint
 * cmd$ rflint path/to/test.robot
 * Created by traitanit on 3/27/2017 AD.
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class RFLintParser extends RegexpLineParser {

    private static final String RFLINT_ERROR_PATTERN = "([W|E|I]): (\\d+), (\\d+): (.*) \\((.*)\\)";
    private static final String RFLINT_FILE_PATTERN = "\\+\\s(.*)";
    private String fileName;

    public RFLintParser(){
        super(Messages._Warnings_RFLint_ParserName(),
                Messages._Warnings_RFLint_LinkName(),
                Messages._Warnings_RFLint_TrendName(),
                RFLINT_ERROR_PATTERN);
    }

    @Override
    public Collection<FileAnnotation> parse(Reader file) throws IOException {
        List<FileAnnotation> warnings = new ArrayList<FileAnnotation>();
        LineIterator iterator = IOUtils.lineIterator(file);
        Pattern filePattern = Pattern.compile(RFLINT_FILE_PATTERN);
        try {
            while (iterator.hasNext()) {
                String line = ConsoleNote.removeNotes(iterator.nextLine());
                // check if line contains file name.
                Matcher matcher = filePattern.matcher(line);
                if (matcher.find()) {
                    fileName = matcher.group(1);
                }
                findAnnotations(line, warnings);
            }
        }
        finally {
            iterator.close();
        }
        return warnings;
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
