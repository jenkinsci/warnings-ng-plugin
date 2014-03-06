package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

/**
 * A parser for Microsoft PREfast XML files.
 *
 * @author Charles Chan
 */
@Extension
public class PREfastParser extends RegexpLineParser {
    private static final long serialVersionUID = 1409381677034028504L;

    private static final String IDENTIFIER_TYPE = "Static Code Analyzer (PREfast)";

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
     * <DEFECT> ... </DEFECT>
     *     - the tag containing 1 violation
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
    private static final String PREFAST_PATTERN_WARNING = "<DEFECT>.*?<FILENAME>(.+?)</FILENAME>.*?<LINE>(.+?)</LINE>.*?<DEFECTCODE>(.+?)</DEFECTCODE>.*?<DESCRIPTION>(.+?)</DESCRIPTION>.*?</DEFECT>";

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
        final String fileName = matcher.group(1);
        final String lineNumber = matcher.group(2);
        final String category = matcher.group(3);
        final String message = matcher.group(4);

        return createWarning(fileName, Integer.parseInt(lineNumber), category, message);
    }
}

