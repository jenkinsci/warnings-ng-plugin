package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

/**
 * A parser for Microsoft PREfast (aka Code Analysis for C/C++) XML files.
 *
 * @author Charles Chan
 */
@Extension
public class PREfastParser extends RegexpLineParser {
    private static final long serialVersionUID = 1409381677034028504L;

    /*
     * Microsoft PREfast static code analyzer produces XML files with the
     * following schema.
     *
     * <?xml version="1.0" encoding="UTF-8"?>
     * <DEFECTS>
     *   <DEFECT _seq="1">
     *     <SFA>
     *       <FILEPATH>d:\myproject\</FILEPATH>
     *       <FILENAME>filename.c</FILENAME>
     *       <LINE>102</LINE>
     *       <COLUMN>9</COLUMN>
     *     </SFA>
     *     <DEFECTCODE>28101</DEFECTCODE>
     *     <DESCRIPTION>A long message</DESCRIPTION>
     *     <FUNCTION>DriverEntry</FUNCTION>
     *     <DECORATED>DriverEntry@8</DECORATED>
     *     <FUNCLINE>102</FUNCLINE>
     *     <PATH/>
     *   </DEFECT>
     *   <DEFECT>
     *     ...
     *   </DEFECT>
     * </DEFECTS>
     *
     * The following regular expression performs the following matches:
     * <DEFECT.*?> ... </DEFECT>
     *     - the tag containing 1 violation (seq number ignored)
     * .*?
     *     - zero or more characters
     * <FILENAME>(.+?)</FILENAME>
     *     - capture group 1 to get the filename
     * <LINE>(.+?)</LINE>
     *     - capture group 2 to get the line number
     * <DEFECTCODE>(.+?)</DEFECTCODE>
     *     - capture group 3 to get the error code
     * <DESCRIPTION>(.+?)</DESCRIPTION>
     *     - capture group 4 to get the description
     */
    private static final String PREFAST_PATTERN_WARNING = "<DEFECT.*?>.*?<FILENAME>(.+?)</FILENAME>.*?<LINE>(.+?)</LINE>.*?<DEFECTCODE>(.+?)</DEFECTCODE>.*?<DESCRIPTION>(.+?)</DESCRIPTION>.*?</DEFECT>";

    /**
     * Creates a new instance of {@link PREfastParser}.
     */
    public PREfastParser() {
        super(Messages._Warnings_PREfast_ParserName(),
                Messages._Warnings_PREfast_LinkName(),
                Messages._Warnings_PREfast_TrendName(),
                PREFAST_PATTERN_WARNING);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(1);
        String lineNumber = matcher.group(2);
        String category = matcher.group(3);
        String message = matcher.group(4);

        return createWarning(fileName, getLineNumber(lineNumber), category, message);
    }
}