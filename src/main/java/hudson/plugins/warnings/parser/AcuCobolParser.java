package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for the Acu Cobol compile.
 *
 * @author jerryshea
 */
public class AcuCobolParser extends RegexpLineParser {
    private static final long serialVersionUID = -894639209290549425L;
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "AcuCobol Compiler";
    /** Pattern of cobol compiler warnings. */
    private static final String ACUCOBOL_WARNING_PATTERN = "^\\s*(\\[.*\\])?\\s*?(.*), line ([0-9]*): Warning: (.*)$";

    /**
     * Creates a new instance of {@link AcuCobolParser}.
     */
    public AcuCobolParser() {
        super(ACUCOBOL_WARNING_PATTERN, WARNING_TYPE, true);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("Warning");
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher
     *            the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(4);
        String category = classifyIfEmpty(StringUtils.EMPTY, message);
        return new Warning(matcher.group(2), getLineNumber(matcher.group(3)), WARNING_TYPE, category, message);
    }
}

