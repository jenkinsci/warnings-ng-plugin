package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link SunCParser}.
 */
public class SunCParserTest extends ParserTester {
    /** Warning message. */
    private static final String MESSAGE = "String literal converted to char* in formal argument 1 in call to userlog(char*, ...).";
    /** Warning category. */
    private static final String TYPE = "badargtypel2w";

    /**
     * Parses a file with 5 warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseSunCpp() throws IOException {
        Collection<FileAnnotation> warnings = new SunCParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 5, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                212,
                MESSAGE,
                "usi_plugin.cpp",
                SunCParser.WARNING_TYPE, TYPE, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                224,
                "String literal converted to char* in formal argument msg in call to except(char*).",
                "usi_plugin.cpp",
                SunCParser.WARNING_TYPE, TYPE, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                8,
                MESSAGE,
                "ServerList.cpp",
                SunCParser.WARNING_TYPE, "", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                44,
                MESSAGE,
                "ServerList.cpp",
                SunCParser.WARNING_TYPE, TYPE, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                50,
                MESSAGE,
                "ServerList.cpp",
                SunCParser.WARNING_TYPE, TYPE, Priority.NORMAL);
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "sunc.txt";
    }
}

