package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link SunCParser}.
 */
public class SunCParserTest extends ParserTester {
    private static final String TYPE = new SunCParser().getGroup();
    private static final String MESSAGE = "String literal converted to char* in formal argument 1 in call to userlog(char*, ...).";
    private static final String CATEGORY = "badargtypel2w";

    /**
     * Parses a file with 8 warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseSunCpp() throws IOException {
        Collection<FileAnnotation> warnings = new SunCParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 8, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                212,
                MESSAGE,
                "usi_plugin.cpp",
                TYPE, CATEGORY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                224,
                "String literal converted to char* in formal argument msg in call to except(char*).",
                "usi_plugin.cpp",
                TYPE, CATEGORY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                8,
                MESSAGE,
                "ServerList.cpp",
                TYPE, "", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                44,
                MESSAGE,
                "ServerList.cpp",
                TYPE, CATEGORY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                50,
                MESSAGE,
                "ServerList.cpp",
                TYPE, CATEGORY, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                19,
                "Child::operator== hides the function Parent::operator==(Parent&) const.",
                "warner.cpp",
                TYPE, "hidef", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                30,
                "Assigning void(*)(int) to extern \"C\" void(*)(int).",
                "warner.cpp",
                TYPE, "wbadlkgasg", Priority.NORMAL);
        annotation = iterator.next();
	// test warning where -errtags=yes was not used
        checkWarning(annotation,
                32,
                "statement is unreachable.",
                "warner.cpp",
                TYPE, "", Priority.NORMAL);
    }

    @Override
    protected String getWarningsFile() {
        return "sunc.txt";
    }
}

