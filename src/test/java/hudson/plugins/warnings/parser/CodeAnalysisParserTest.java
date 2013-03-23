package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link CodeAnalysisParser}.
 */
public class CodeAnalysisParserTest extends ParserTester {
    /**
     * Parses a file with warnings of the CodeAnalysis tools.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseWarnings() throws IOException {
        Collection<FileAnnotation> warnings = new CodeAnalysisParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                0,
                "Microsoft.Performance : It appears that field 'Program.a' is never used or is only ever assigned to. Use this field or remove it.",
                "C:/Src/Parser/CSharp/Test.csproj",
                CodeAnalysisParser.WARNING_TYPE, "CA1823", Priority.NORMAL);
		annotation = iterator.next();
        checkWarning(annotation,
                140,
                "Microsoft.Design : Modify 'AccountController.ChangePassword(ChangePasswordModel)' to catch a more specific exception than 'Exception' or rethrow the exception.",
                "C:/Src/Parser/CSharp/test.cs",
                CodeAnalysisParser.WARNING_TYPE, "CA1031", Priority.NORMAL);
    }

    @Override
    protected String getWarningsFile() {
        return "codeanalysis.txt";
    }
}

