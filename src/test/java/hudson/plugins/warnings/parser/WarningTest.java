package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;
import hudson.plugins.analysis.core.AnnotationDifferencer;
import hudson.plugins.analysis.util.model.FileAnnotation;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Tests the class {@link Warning}.
 *
 * @author Ulli Hafner
 */
public class WarningTest {
    /**
     * Simple equals test.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-14821">Issue 14821</a>
     */
    @Test
    public void testEquals() {
        Warning expected = new Warning("filename", 0, "type", "category", "message");
        expected.setContextHashCode(1);

        Warning actual = new Warning("filename", 0, "type", "category", "message");
        expected.setContextHashCode(2);

        assertEquals("The objects are not the same", expected, actual);

        Set<FileAnnotation> current = new HashSet<FileAnnotation>();
        current.add(expected);

        Set<FileAnnotation> reference = new HashSet<FileAnnotation>();
        reference.add(actual);

        Set<FileAnnotation> annotations = AnnotationDifferencer.getNewAnnotations(current, reference);
        assertTrue("There are new warnings", annotations.isEmpty());
    }
}

