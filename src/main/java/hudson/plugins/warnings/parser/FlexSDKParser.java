package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

/**
 * A parser for Flex SDK compiler warnings.
 *
 * @author Vivien Tintillier
 */
public class FlexSDKParser extends RegexpLineParser {
    private static final long serialVersionUID = -185055018399324311L;
    private static final String FLEX_SDK_WARNING_PATTERN = "^\\s*(?:\\[.*\\])?\\s*(.*\\.as|.*\\.mxml)\\((\\d*)\\):\\s*(?:col:\\s*\\d*\\s*)?(?:Warning)\\s*:\\s*(.*)$";

    /**
     * Creates a new instance of {@link FlexSDKParser}.
     */
    public FlexSDKParser() {
        super(Messages._Warnings_Flex_ParserName(),
                Messages._Warnings_Flex_LinkName(),
                Messages._Warnings_Flex_TrendName(),
                FLEX_SDK_WARNING_PATTERN, true);
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("Warning");
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), matcher.group(3));
    }
}

