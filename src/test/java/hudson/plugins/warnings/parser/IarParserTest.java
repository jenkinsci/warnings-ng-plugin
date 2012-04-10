package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link IarParser}.
 *
 * @author Claus Klein
 */
public class IarParserTest extends ParserTester {
    private static final String TYPE = new IarParser().getGroup();

    /**
     * Parses a file with two IAR warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new IarParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 8, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                451,
                "`void yyunput(int, char*)' defined but not used",
                "testhist.l",
                TYPE, "Pe0815", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                73,
                "implicit typename is deprecated, please see the documentation for details",
                "/u1/drjohn/bfdist/packages/RegrTest/V00-03-01/RgtAddressLineScan.cc",
                TYPE, "Pe0815", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                39,
                "foo.h: No such file or directory",
                "foo.cc",
                TYPE, "Pe0815", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                56,
                "type qualifier on return type is meaningless",
                "D:/src/CSpiBus.h",
                TYPE, "Pe815", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                38,
                "identifier \"OS_EnterNestableInterrupt\" is undefined",
                "D:/src/InterruptTabO7.cpp",
                TYPE, "Pe020", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                229,
                "variable \"ret\" was set but never used",
                "z:/src/O7_LabSample_embOS/EmbosConfig/CANopen/main.cpp",
                TYPE, "Pe550", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                525,
                "variable \"ret\" was set but never used",
                "z:/src/O7_LabSample_embOS/EmbosConfig/CANopen/main.cpp",
                TYPE, "Pe550", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                78,
                "missing return statement at end of non-void function \"CPwmOutput::setDutyCycle\"",
                "z:/src/O7_LabSample_embOS/EmbosConfig/CPwmOutput.cpp",
                TYPE, "Pe940", Priority.NORMAL);
    }

    @Override
    protected String getWarningsFile() {
        return "iar.txt";
    }
}

