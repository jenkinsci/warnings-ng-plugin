package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.plugins.analysis.util.model.Priority;

import hudson.Extension;

/**
 * A parser for the PyLint compiler warnings.
 *
 * @author Sebastian Hansbauer
 */
@Extension
public class PyLintParser extends RegexpLineParser {
    private static final long serialVersionUID = 4464053085862883240L;

    private static final String PYLINT_ERROR_PATTERN = "(.*):(\\d+): \\[(\\D\\d*).*\\] (.*)";

    /**
     * Creates a new instance of {@link PyLintParser}.
     */
    public PyLintParser() {
        super(Messages._Warnings_PyLint_ParserName(),
                Messages._Warnings_PyLint_LinkName(),
                Messages._Warnings_PyLint_TrendName(),
                PYLINT_ERROR_PATTERN, true);
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("[");
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(4);
        String category = classifyIfEmpty(matcher.group(3), message);
        //First letter of the Pylint classification is one of F/E/W/R/C. E/F/W are high priority.
        Priority priority = Priority.LOW;

        // See http://docs.pylint.org/output.html for definitions of the categories
        switch (category.charAt(0)) {
          // [R]efactor for a “good practice” metric violation
          // [C]onvention for coding standard violation
        case 'R':
        case 'C':
          priority = Priority.LOW;
        break;
        
        // [W]arning for stylistic problems, or minor programming issues
        case 'W':
          priority = Priority.NORMAL;
          break;
          
          // [E]rror for important programming issues (i.e. most probably bug)
          // [F]atal for errors which prevented further processing
        case 'E':
        case 'F':
          priority = Priority.HIGH;
        break;
        }

        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), category, message, priority);
    }
}
