package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for (compile-time) messages from the GNU Fortran Compiler.
 *
 * @author Mat Cross.
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class GnuFortranParser extends RegexpDocumentParser {
    private static final String LINE_REGEXP = " at \\(\\d\\)";
    private static final long serialVersionUID = 0L;
    /**
     * The gfortran regex string that follows has been reverse engineered from the show_locus function in
     * gcc/fortran/error.c at r204295. By inspection of the GCC release branches this regex should be compatible with
     * GCC 4.2 and newer.
     */
    private static final String GFORTRAN_MSG_PATTERN = "(?s)^([^\\n]+\\.[^:\\n]+):(\\d+)" // file:line.
            + "(?:\\.(\\d+)(-\\d+)?)?" // Optional column (with optional range).
            + ":\\n" + "(?:    Included at [^\\n]+\\n)*" // Optional "    Included at file:line:", any number of times.
            + "\\n" + "[^\\n]+\\n" // The offending line itself. NOCHECKSTYLE
            + "[^\\n]+\\n" // The '1' and/or '2' corresponding to the column of the error locus.
            + "(Warning|Error|Fatal Error|Internal Error at \\(1\\)):[\\s\\n]([^\\n]+)\\n";

    /**
     * Creates a new instance of {@link GnuFortranParser}.
     */
    public GnuFortranParser() {
        super(Messages._Warnings_GnuFortran_ParserName(), Messages._Warnings_GnuFortran_LinkName(), Messages
                ._Warnings_GnuFortran_TrendName(), GFORTRAN_MSG_PATTERN, true);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String columnStart = matcher.group(3);
        String columnEnd = matcher.group(4);
        String category = matcher.group(5).replaceAll(LINE_REGEXP, "");
        Priority priority = "Warning".equals(category) ? Priority.NORMAL : Priority.HIGH;
        Warning warning = createWarning(matcher.group(1), getLineNumber(matcher.group(2)), category,
                matcher.group(6).replaceAll(LINE_REGEXP, ""), priority);

        if (StringUtils.isNotEmpty(columnStart)) {
            if (StringUtils.isNotEmpty(columnEnd)) {
                warning.setColumnPosition(getLineNumber(columnStart), getLineNumber(columnEnd));
            }
            else {
                warning.setColumnPosition(getLineNumber(columnStart));
            }
        }
        return warning;
    }
}
