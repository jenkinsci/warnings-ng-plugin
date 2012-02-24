package hudson.plugins.analysis.util.model;

import static org.junit.Assert.*;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;


/**
 * Tests the class {@link WorkspaceFile}.
 *
 * @author Ulli Hafner
 */
public class WorkspaceFileTest {
    /**
     * Verifies that the number of warnings is correctly computed.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-6139">Issue 6139</a>
     */
    @Test
    public void issue6139() {
        WorkspaceFile file = new WorkspaceFile("test");

        file.addAnnotation(new Warning(Priority.HIGH));
        file.addAnnotation(new Warning(Priority.HIGH));
        file.addAnnotation(new Warning(Priority.NORMAL));
        file.addAnnotation(new Warning(Priority.LOW));
        file.addAnnotation(new Warning(Priority.NORMAL));
        file.addAnnotation(new Warning(Priority.LOW));
        file.addAnnotation(new Warning(Priority.NORMAL));
        file.addAnnotation(new Warning(Priority.LOW));
        file.addAnnotation(new Warning(Priority.NORMAL));

        assertEquals("Wrong number of totals", 2, file.getNumberOfHighAnnotations());
        assertEquals("Wrong number of totals", 3, file.getNumberOfLowAnnotations());
        assertEquals("Wrong number of totals", 4, file.getNumberOfNormalAnnotations());
    }

    /**
     * Concrete warning for the test.
     *
     * @author Ulli Hafner
     */
    @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
    private static class Warning extends AbstractAnnotation {
        /** ID. */
        private static final long serialVersionUID = 1L;
        /** Warning key. */
        private static int key = 1;

        /**
         * Creates a new instance of <code>Warning</code>.
         *
         * @param priority
         *            the priority
         */
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("ST")
        Warning(final Priority priority) {
            super(priority, String.valueOf(key++), key++, key++, String.valueOf(key++), String.valueOf(key++));

            setFileName("file");
            key++;
        }

        /** {@inheritDoc} */
        public String getToolTip() {
            return StringUtils.EMPTY;
        }
    }
}

