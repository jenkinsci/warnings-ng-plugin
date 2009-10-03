package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link InvalidsParser}.
 */
public class InvalidsParserTest extends ParserTester {
    /**
     * Creates a new instance of {@link InvalidsParserTest}.
     */
    public InvalidsParserTest() {
        super(InvalidsParser.class);
    }

    /**
     * Parses a file with two deprecation warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testParser() throws IOException {
        Collection<FileAnnotation> warnings = new InvalidsParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 3, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        String type = "Oracle Invalid";
        checkWarning(annotation,
                45,
                "Encountered the symbol \"END\" when expecting one of the following:",
                "ENV_UTIL#.PACKAGE BODY", type, "PLW-05004", Priority.NORMAL);
        assertEquals("wrong schema detected", "E", annotation.getPackageName());
        annotation = iterator.next();
        checkWarning(annotation,
                5,
                "Encountered the symbol \"END\" when expecting one of the following:",
                "ENV_ABBR#B.TRIGGER", type, "PLW-07202", Priority.LOW);
        assertEquals("wrong schema detected", "E", annotation.getPackageName());
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "referenced name javax/management/MBeanConstructorInfo could not be found",
                "/b77ce675_LoggerDynamicMBean.JAVA CLASS", type, "ORA-29521", Priority.HIGH);
        assertEquals("wrong schema detected", "E", annotation.getPackageName());
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "invalids.txt";
    }
}

