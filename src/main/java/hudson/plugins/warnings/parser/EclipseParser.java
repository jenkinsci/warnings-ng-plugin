package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;


/**
 * A parser for Eclipse compiler warnings.
 *
 * @author Ullrich Hafner
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class EclipseParser extends RegexpDocumentParser {
    private static final long serialVersionUID = 425883472788422955L;
    private static final String ANT_ECLIPSE_WARNING_PATTERN =
            "\\[?(WARNING|ERROR)\\]?" +      // group 1 'type': WARNING or ERROR in optional []
            "\\s*(?:in)?" +                  // optional " in"
            "\\s*(.*)" +                     // group 2 'filename'
            "(?:\\(at line\\s*(\\d+)\\)|" +  // either group 3 'lineNumber': at line dd
            ":\\[(\\d+)).*" +                // or group 4 'rowNumber': eg :[row,col] - col ignored
            "(?:\\r?\\n[^\\^\\n]*){1,3}" +   // 1 or 3 ignored lines (no column pointer) eg source excerpt
            "\\r?\\n(.*)" +                  // newline then group 5 (indent for column pointers)
            "([\\^]+).*" +                   // group 6 column pointers (^^^^^)
            "\\r?\\n(?:\\s*\\[.*\\]\\s*)?" + // newline then optional ignored text in [] (eg [javac])
            "(.*)";                          // group 7 'message'

    /**
     * Creates a new instance of {@link EclipseParser}.
     */
    public EclipseParser() {
        super(Messages._Warnings_EclipseParser_ParserName(),
                Messages._Warnings_EclipseParser_LinkName(),
                Messages._Warnings_EclipseParser_TrendName(),
                ANT_ECLIPSE_WARNING_PATTERN, true);
    }

    @Override
    protected String getId() {
        return "Eclipse Java Compiler";
    }

    @Override
    public String getSmallImage() {
        return JavacParser.JAVA_SMALL_ICON;
    }

    @Override
    public String getLargeImage() {
        return JavacParser.JAVA_LARGE_ICON;
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String type = StringUtils.capitalize(matcher.group(1));
        Priority priority;
        if ("warning".equalsIgnoreCase(type)) {
            priority = Priority.NORMAL;
        }
        else {
            priority = Priority.HIGH;
        }
        Warning warning = createWarning(matcher.group(2), getLineNumber(getLine(matcher)), matcher.group(7), priority);

        int columnStart = StringUtils.defaultString(matcher.group(5)).length();
        int columnEnd = columnStart + matcher.group(6).length();
        warning.setColumnPosition(columnStart, columnEnd);

        return warning;
    }

    private String getLine(final Matcher matcher) {
        String eclipse34 = matcher.group(3);
        String eclipse38 = matcher.group(4);

        return StringUtils.defaultIfEmpty(eclipse34, eclipse38);
    }
}

