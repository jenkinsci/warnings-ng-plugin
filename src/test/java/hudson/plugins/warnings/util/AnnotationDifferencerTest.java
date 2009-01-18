package hudson.plugins.warnings.util;

import static org.junit.Assert.*;
import hudson.plugins.warnings.util.model.AbstractAnnotation;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Tests the class {@link AnnotationDifferencer}.
 */
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public abstract class AnnotationDifferencerTest {
    /** Error message. */
    private static final String ANNOTATIONS_ARE_NOT_EQUAL = "Annotations are not equal.";
    /** Filename of annotation. */
    private static final String FILE_NAME = "File.c";
    /** String for comparison. */
    private static final String STRING = "type1";
    /** Indicates a wrong calculation of warnings. */
    private static final String WARNINGS_COUNT_ERROR = "Wrong warnings count.";

    /**
     * Creates a new annotation.
     *
     * @param fileName
     *            filename of the annotation
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     * @param start
     *            the first line of the line range
     * @param end
     *            the last line of the line range
     * @return the created annotation
     */
    public abstract FileAnnotation createAnnotation(String fileName, final Priority priority, final String message, final String category,
            final String type, final int start, final int end);

    /**
     * Checks whether equals works for warnings.
     */
    @Test
    public void testAnnotationFilename() {
        FileAnnotation annotation  = createAnnotation("C:\\Path\\File.c", Priority.HIGH, STRING, STRING, STRING, 2, 3);

        assertEquals(ANNOTATIONS_ARE_NOT_EQUAL, "C:/Path/File.c", annotation.getFileName());
        assertEquals(ANNOTATIONS_ARE_NOT_EQUAL, FILE_NAME, annotation.getShortFileName());

        annotation  = createAnnotation("/Path/File.c", Priority.HIGH, STRING, STRING, STRING, 2, 3);

        assertEquals(ANNOTATIONS_ARE_NOT_EQUAL, "/Path/File.c", annotation.getFileName());
        assertEquals(ANNOTATIONS_ARE_NOT_EQUAL, FILE_NAME, annotation.getShortFileName());

        annotation  = createAnnotation("/File.c", Priority.HIGH, STRING, STRING, STRING, 2, 3);

        assertEquals(ANNOTATIONS_ARE_NOT_EQUAL, "/File.c", annotation.getFileName());
        assertEquals(ANNOTATIONS_ARE_NOT_EQUAL, FILE_NAME, annotation.getShortFileName());

        annotation  = createAnnotation(FILE_NAME, Priority.HIGH, STRING, STRING, STRING, 2, 3);

        assertEquals(ANNOTATIONS_ARE_NOT_EQUAL, FILE_NAME, annotation.getFileName());
        assertEquals(ANNOTATIONS_ARE_NOT_EQUAL, FILE_NAME, annotation.getShortFileName());
    }

    /**
     * Checks whether equals works for warnings.
     */
    @Test
    public void testWarningEquals() {
        FileAnnotation first  = createAnnotation(STRING, Priority.HIGH, STRING, STRING, STRING, 2, 3);
        FileAnnotation second = createAnnotation(STRING, Priority.HIGH, STRING, STRING, STRING, 2, 3);

        assertEquals(ANNOTATIONS_ARE_NOT_EQUAL, first, second);

        FileAnnotation third = createAnnotation(STRING, Priority.HIGH, "other", STRING, STRING, 2, 3);

        assertFalse("Annotations are equal.", first.equals(third));

        third = createAnnotation(STRING, Priority.HIGH, STRING, STRING, STRING, 3, 2);

        assertFalse("Annotations are equal.", first.equals(third));
    }

    /**
     * Checks whether differencing detects single changes (new and fixed).
     */
    @Test
    public void testDifferencer() {
        Set<FileAnnotation> actual = new HashSet<FileAnnotation>();
        Set<FileAnnotation> previous = new HashSet<FileAnnotation>();

        FileAnnotation annotation = createAnnotation(STRING, Priority.HIGH, STRING, STRING, STRING, 2, 3);
        actual.add(annotation);

        annotation = createAnnotation(STRING, Priority.HIGH, STRING, STRING, STRING, 2, 3);
        previous.add(annotation);

        assertEquals(WARNINGS_COUNT_ERROR, 0, AnnotationDifferencer.getFixedAnnotations(actual, previous).size());

        annotation = createAnnotation(STRING, Priority.HIGH, "type2", STRING, STRING, 2, 3);
        previous.add(annotation);

        assertEquals(WARNINGS_COUNT_ERROR, 0, AnnotationDifferencer.getNewAnnotations(actual, previous).size());
        assertEquals(WARNINGS_COUNT_ERROR, 1, AnnotationDifferencer.getFixedAnnotations(actual, previous).size());

        annotation = createAnnotation(STRING, Priority.HIGH, "type2", STRING, STRING, 2, 3);
        actual.add(annotation);

        assertEquals(WARNINGS_COUNT_ERROR, 0, AnnotationDifferencer.getNewAnnotations(actual, previous).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, AnnotationDifferencer.getFixedAnnotations(actual, previous).size());

        annotation = createAnnotation(STRING, Priority.HIGH, "type3", STRING, STRING, 2, 3);
        actual.add(annotation);

        assertEquals(WARNINGS_COUNT_ERROR, 1, AnnotationDifferencer.getNewAnnotations(actual, previous).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, AnnotationDifferencer.getFixedAnnotations(actual, previous).size());
    }

    /**
     * Checks whether the hash codes are evaluated if similar warnings are part of new and fixed.
     */
    @Test
    public void testHashCodes() {
        Set<FileAnnotation> actual = new HashSet<FileAnnotation>();
        Set<FileAnnotation> previous = new HashSet<FileAnnotation>();

        FileAnnotation current = createAnnotation(STRING, Priority.HIGH, STRING, STRING, STRING, 3, 4);
        actual.add(current);

        FileAnnotation old = createAnnotation(STRING, Priority.HIGH, STRING, STRING, STRING, 2, 3);
        previous.add(old);

        assertEquals(WARNINGS_COUNT_ERROR, 1, AnnotationDifferencer.getFixedAnnotations(actual, previous).size());
        assertEquals(WARNINGS_COUNT_ERROR, 1, AnnotationDifferencer.getFixedAnnotations(actual, previous).size());

        ((AbstractAnnotation)current).setContextHashCode(0);
        ((AbstractAnnotation)old).setContextHashCode(0);

        assertEquals(WARNINGS_COUNT_ERROR, 0, AnnotationDifferencer.getFixedAnnotations(actual, previous).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, AnnotationDifferencer.getFixedAnnotations(actual, previous).size());
    }
}

