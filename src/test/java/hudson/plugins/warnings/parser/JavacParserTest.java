package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link JavacParser}.
 */
public class JavacParserTest extends ParserTester {
    /**
     * Creates a new instance of {@link JavacParserTest}.
     */
    public JavacParserTest() {
        super(JavacParser.class);
    }

    /**
     * Parses a file with two deprecation warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseDeprecation() throws IOException {
        Collection<FileAnnotation> warnings = new JavacParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                12,
                "org.eclipse.jface.contentassist.SubjectControlContentAssistant in org.eclipse.jface.contentassist has been deprecated",
                "C:/Build/Results/jobs/ADT-Base/workspace/com.avaloq.adt.ui/src/main/java/com/avaloq/adt/ui/elements/AvaloqDialog.java",
                JavacParser.WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                40,
                "org.eclipse.ui.contentassist.ContentAssistHandler in org.eclipse.ui.contentassist has been deprecated",
                "C:/Build/Results/jobs/ADT-Base/workspace/com.avaloq.adt.ui/src/main/java/com/avaloq/adt/ui/elements/AvaloqDialog.java",
                JavacParser.WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "javac.txt";
    }
}

