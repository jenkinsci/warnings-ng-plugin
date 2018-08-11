package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

/**
 * A parser for the go vet tool in the Go toolchain
 *
 * @author Ryan Cox
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class GoVetParser extends GoBaseParser {

    private static final long serialVersionUID = 1451787851164850844L;

    // ui_colored_test.go:59: missing argument for Fatalf("%#v"): format reads arg 2, have only 1 args
    private static final String GOVET_WARNING_PATTERN = "^(.+?):(\\d+?):\\s*(.*)$";

    /**
     * Creates a new instance of {@link GoVetParser}.
     */
    public GoVetParser() {

        super(Messages._Warnings_GoVetParser_ParserName(),
                Messages._Warnings_GoVetParser_LinkName(),
                Messages._Warnings_GoVetParser_TrendName(),
                GOVET_WARNING_PATTERN, true);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(3);
        String category = classifyIfEmpty("", message);

        Warning warning = createWarning(matcher.group(1), getLineNumber(matcher.group(2)), category, message);
        return warning;
    }
}
