package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;
import hudson.plugins.analysis.core.AnnotationDifferencer;
import hudson.plugins.analysis.util.TreeString;
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
    public void testEqualsIssue14821() {
        Warning expected = createWarning();
        expected.setContextHashCode(1);

        Warning actual = createWarning();
        expected.setContextHashCode(2);

        assertEquals("The objects are not the same", expected, actual);

        Set<FileAnnotation> current = new HashSet<FileAnnotation>();
        current.add(expected);

        Set<FileAnnotation> reference = new HashSet<FileAnnotation>();
        reference.add(actual);

        Set<FileAnnotation> annotations = AnnotationDifferencer.getNewAnnotations(current, reference);
        assertTrue("There are new warnings", annotations.isEmpty());
    }

    /**
     * Verifies that we correctly handle <code>null</code> values in {@link TreeString} instances.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-15250">Issue 15250</a>
     */
    @Test
    public void testEqualsIssue15250() {
        Warning first = createWarning();
        Warning second = createWarning();

        assertEquals("The objects are not the same", first, second);
        first.setPackageName("something");
        assertFalse("The objects are the same", first.equals(second));
        first.setModuleName("something");
        assertFalse("The objects are the same", first.equals(second));
    }

    private Warning createWarning() {
        return new Warning("filename", 0, "type", "category", "message");
    }
}

