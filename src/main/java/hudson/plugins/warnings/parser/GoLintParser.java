package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

/**
 * A parser for the golint tool in the Go toolchain
 *
 * @author Ryan Cox
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class GoLintParser extends GoBaseParser {

    private static final long serialVersionUID = -5895416507693444713L;

    // conn.go:360:3: should replace c.writeSeq += 1 with c.writeSeq++
    private static final String GOLINT_WARNING_PATTERN = "^(.*?):(\\d+?):(\\d*?):\\s*(.*)$";

    /**
     * Creates a new instance of {@link GoLintParser}.
     */
    public GoLintParser() {

        super(Messages._Warnings_GoLintParser_ParserName(),
                Messages._Warnings_GoLintParser_LinkName(),
                Messages._Warnings_GoLintParser_TrendName(),
                GOLINT_WARNING_PATTERN, true);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(4);
        String category = classifyIfEmpty("", message);

        Warning warning = createWarning(matcher.group(1), getLineNumber(matcher.group(2)), category, message);
        warning.setColumnPosition(getLineNumber(matcher.group(3)));
        return warning;
    }
}

