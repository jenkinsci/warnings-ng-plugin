package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

/**
 * A parser for the ansible lint warnings.
 *
 * @author Ce Qi
 */
@Extension
public class AnsibleLintParser extends RegexpLineParser {

    private static final long serialVersionUID = 8481090596321427484L;
    private static final String ANSIBLE_LINT_WARNING_PATTERN = "(.*)\\:([0-9]*)\\:\\s*\\[.*(ANSIBLE[0-9]*)\\]\\s(.*)";

    /**
     * Creates a new instance of {@link AnsibleLintParser}
     */
    public AnsibleLintParser(){
        super(Messages._Warnings_AnsibleLint_ParserName(),
                Messages._Warnings_AnsibleLint_LinkName(),
                Messages._Warnings_AnsibleLint_TrendName(),
                ANSIBLE_LINT_WARNING_PATTERN,true);
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("[");
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(1);
        String lineNumber = matcher.group(2);
        String category = matcher.group(3);
        String message = matcher.group(4);
        Warning warning  = createWarning(fileName,getLineNumber(lineNumber),category,message);
        return warning;

    }
}

