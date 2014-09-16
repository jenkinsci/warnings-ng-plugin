package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.warnings.WarningsDescriptor;

/**
 * A parser for the go vet tool in the Go toolchain
 *
 * @author Ryan Cox
 */
@Extension
public class GoVetParser extends RegexpLineParser {


    private static final long serialVersionUID = 1451787851164850844L;
    static final String GO_SMALL_ICON = WarningsDescriptor.IMAGE_PREFIX + "go-24x24.png";
    static final String GO_LARGE_ICON = WarningsDescriptor.IMAGE_PREFIX + "go-48x48.png";

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

    @Override
    public String getSmallImage() {
        return GO_SMALL_ICON;
    }

    @Override
    public String getLargeImage() {
        return GO_LARGE_ICON;
    }

    @Override
    protected String getId() {
        return "Go Vet";
    }
}

