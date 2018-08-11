package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

/**
 * A parser for the Acu Cobol compile.
 *
 * @author jerryshea
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class AcuCobolParser extends RegexpLineParser {
    private static final long serialVersionUID = -894639209290549425L;
    private static final String ACUCOBOL_WARNING_PATTERN = "^\\s*(\\[.*\\])?\\s*?(.*), line ([0-9]*): Warning: (.*)$";

    /**
     * Creates a new instance of {@link AcuCobolParser}.
     */
    public AcuCobolParser() {
        super(Messages._Warnings_AcuCobol_ParserName(),
                Messages._Warnings_AcuCobol_LinkName(),
                Messages._Warnings_AcuCobol_TrendName(),
                ACUCOBOL_WARNING_PATTERN, true);
    }

    @Override
    protected String getId() {
        return "AcuCobol Compiler";
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("Warning");
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(4);
        String category = classifyWarning(message);
        return new Warning(matcher.group(2), getLineNumber(matcher.group(3)), getGroup(), category, message);
    }
}

