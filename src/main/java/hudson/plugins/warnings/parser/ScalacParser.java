package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

/**
 * A parser for the javac compiler warnings.
 * You should use -feature & -deprecation compiler opts.
 *
 * @author <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 */
@Extension
public class ScalacParser extends RegexpLineParser {

    private static final String JAVAC_WARNING_PATTERN = "^(\\[WARNING\\]|\\[ERROR\\])\\s*(.*):(\\d+):\\s*([a-z]*):\\s*(.*)$";

    /**
     * Creates a new instance of {@link ScalacParser}.
     */
    public ScalacParser() {
        super(Messages._Warnings_ScalaParser_ParserName(),
                Messages._Warnings_ScalaParser_LinkName(),
                Messages._Warnings_ScalaParser_TrendName(),
                JAVAC_WARNING_PATTERN, true);
    }

    @Override
    protected Warning createWarning(Matcher matcher) {
        String fileName = matcher.group(2);
        String lineNumber = matcher.group(3);
        String category = matcher.group(4);
        String message = matcher.group(5);
        return createWarning(fileName, getLineNumber(lineNumber), category, message);
    }
}
