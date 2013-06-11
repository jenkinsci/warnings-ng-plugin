package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the Coolflux DSP Compiler warnings.
 *
 * @author Vangelis Livadiotis
 */
@Extension
public class CoolfluxChessccParser extends RegexpLineParser {
    private static final long serialVersionUID = 4742509996511002391L;
    private static final String CHESSCC_PATTERN = "^.*?Warning in \"([^\"]+)\", line (\\d+),.*?:\\s*(.*)$";

    /**
     * Creates a new instance of {@link CoolfluxChessccParser}.
     */
    public CoolfluxChessccParser() {
        super(Messages._Warnings_Coolflux_ParserName(),
                Messages._Warnings_Coolflux_LinkName(),
                Messages._Warnings_Coolflux_TrendName(),
                CHESSCC_PATTERN, true);
    }

    @Override
    protected String getId() {
        return "Coolflux DSP Compiler";
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("Warning");
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), matcher.group(3), Priority.HIGH);
    }
}


