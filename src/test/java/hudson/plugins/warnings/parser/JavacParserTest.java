package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link JavacParser}.
 */
public class JavacParserTest extends ParserTester {
    private static final String WARNING_TYPE = Messages._Warnings_JavaParser_ParserName().toString(Locale.ENGLISH);

    /**
     * Parses a warning log with two false positives.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-14043">Issue 14043</a>
     */
    @Test
    public void issue14043() throws IOException {
        Collection<FileAnnotation> warnings = parse("issue14043.txt");

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 0, warnings.size());
    }

    /**
     * Parses a warning log with 15 warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-12482">Issue 12482</a>
     */
    @Test
    public void issue12482() throws IOException {
        Collection<FileAnnotation> java6 = parse("issue12482-java6.txt");

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 62, java6.size());

        Collection<FileAnnotation> java7 = parse("issue12482-java7.txt");

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 62, java7.size());
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

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                12, 39,
                "org.eclipse.jface.contentassist.SubjectControlContentAssistant in org.eclipse.jface.contentassist has been deprecated",
                "C:/Build/Results/jobs/ADT-Base/workspace/com.avaloq.adt.ui/src/main/java/com/avaloq/adt/ui/elements/AvaloqDialog.java",
                WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                40, 36,
                "org.eclipse.ui.contentassist.ContentAssistHandler in org.eclipse.ui.contentassist has been deprecated",
                "C:/Build/Results/jobs/ADT-Base/workspace/com.avaloq.adt.ui/src/main/java/com/avaloq/adt/ui/elements/AvaloqDialog.java",
                WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);
    }

    /**
     * Verifies that arrays in deprecated methods are correctly handled.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void parseArrayInDeprecatedMethod() throws IOException {
        Collection<FileAnnotation> warnings = parse("issue5868.txt");

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                14,
                "loadAvailable(java.lang.String,int,int,java.lang.String[]) in my.OtherClass has been deprecated",
                "D:/path/to/my/Class.java",
                WARNING_TYPE, "Deprecation", Priority.NORMAL);
    }

    private Collection<FileAnnotation> parse(final String fileName) throws IOException {
        return new JavacParser().parse(openFile(fileName));
    }

    @Override
    protected String getWarningsFile() {
        return "javac.txt";
    }
}

