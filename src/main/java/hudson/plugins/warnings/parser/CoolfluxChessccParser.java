package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the Coolflux DSP Compiler warnings.
 *
 * @author Vangelis Livadiotis
 */
public class CoolfluxChessccParser extends RegexpLineParser {
    private static final long serialVersionUID = 4742509996511002391L;
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Coolflux DSP Compiler (chesscc)";
    /** Pattern of Intel compiler warnings. */
    private static final String CHESSCC_PATTERN = "^.*?Warning in \"([^\"]+)\", line (\\d+),.*?:\\s*(.*)$";

    /**
     * Creates a new instance of <code>InterCParser</code>.
     */
    public CoolfluxChessccParser() {
        super(CHESSCC_PATTERN, "Coolflux DSP Compiler", true);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("Warning");
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE,
                "Warning", matcher.group(3), Priority.HIGH);
    }
}


