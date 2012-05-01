package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static junit.framework.Assert.*;
import org.junit.Test;


/**
* Tests the class {@link AppleLLVMClangParser}.
* @author Neil Davis
 */
public class AppleLLVMClangParserTest  extends ParserTester{
    private static final String TYPE = new AppleLLVMClangParser().getGroup();
    
    public AppleLLVMClangParserTest() {
    }

    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new AppleLLVMClangParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 8, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                28,
                "extra tokens at end of #endif directive",
                "test.c",
                TYPE, "-Wextra-tokens", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                28,
                "extra tokens at end of #endif directive",
                "/path/to/test.c",
                TYPE, "-Wextra-tokens", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                128,
                "extra tokens at end of #endif directive",
                "test.c",
                TYPE, "-Wextra-tokens", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                28,
                "extra tokens at end of #endif directive",
                "test.c",
                TYPE, "", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                3,
                "conversion specifies type 'char *' but the argument has type 'int'",
                "t.c",
                TYPE, "-Wformat", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                3,
                "conversion specifies type 'char *' but the argument has type 'int'",
                "t.c",
                TYPE, "-Wformat,1", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                3,
                "conversion specifies type 'char *' but the argument has type 'int'",
                "t.c",
                TYPE, "-Wformat,Format String", Priority.NORMAL);
        
        annotation = iterator.next();
        checkWarning(annotation,
                47,
                "invalid operands to binary expression ('int *' and '_Complex float')",
                "exprs.c",
                TYPE, "", Priority.NORMAL);
                 
        
     }


    @Override
    protected String getWarningsFile() {
        return "apple-llvm-clang.txt";
     }
}
