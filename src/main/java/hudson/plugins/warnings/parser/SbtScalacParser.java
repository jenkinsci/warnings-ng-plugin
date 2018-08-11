package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.WarningsDescriptor;

/**
 * A parser for the sbt scala compiler warnings.
 * You should use -feature and -deprecation compiler opts.
 *
 * @author Hochak Hung
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class SbtScalacParser extends RegexpLineParser {
    private static final String SCALA_SMALL_ICON = WarningsDescriptor.IMAGE_PREFIX + "scala-24x24.png";
    private static final String SCALA_LARGE_ICON = WarningsDescriptor.IMAGE_PREFIX + "scala-48x48.png";

    private static final String SBT_WARNING_PATTERN = "^(\\[warn\\]|\\[error\\])\\s*(.*):(\\d+):\\s*(.*)$";

    /**
     * Creates a new instance of {@link SbtScalacParser}.
     */
    public SbtScalacParser() {
        super(Messages._Warnings_ScalaParser_ParserName(),
                Messages._Warnings_ScalaParser_LinkName(),
                Messages._Warnings_ScalaParser_TrendName(),
                SBT_WARNING_PATTERN, true);
    }

    @Override
    protected Warning createWarning(Matcher matcher) {
        Priority p = matcher.group(1).equals("[error]") ? Priority.HIGH : Priority.NORMAL;
        String fileName = matcher.group(2);
        String lineNumber = matcher.group(3);
        String message = matcher.group(4);
        return createWarning(fileName, getLineNumber(lineNumber), message, p);
    }

    @Override
    public String getSmallImage() {
        return SCALA_SMALL_ICON;
    }

    @Override
    public String getLargeImage() {
        return SCALA_LARGE_ICON;
    }
}
