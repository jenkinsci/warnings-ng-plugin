package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import hudson.plugins.analysis.util.NullLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link WarningsFilter}.
 *
 * @author StKlug
 */
public class WarningsFilterTest {
    /**
     * Tests the exclusion of certain warning messages from the report.
     */
    @Test
    public void testMessagesPattern() {
        Warning w1 = createDummyWarning("Javadoc: Missing tag for parameter arg1");
        Warning w2 = createDummyWarning("The import java.io.OutputStream is never used");
        Collection<FileAnnotation> warnings = new LinkedList<FileAnnotation>();
        warnings.add(w1);
        warnings.add(w2);

        WarningsFilter filter = new WarningsFilter();
        // exclude warnings with this warning message from the report
        final String excludeMessage = "Javadoc: Missing tag for parameter arg1";
        warnings = filter.apply(warnings, null, null, excludeMessage, null, new NullLogger());

        assertFalse(warnings.contains(w1));
        assertTrue(warnings.contains(w2));
    }

    /**
     * Tests the exclusion of certain warning messages from the report (based on category).
     */
    @Test
    public void testCategoriesPattern() {
        Warning w1 = new Warning("dummyFile.java", 0, "warningType", "-W#pragma-messages", "Warning. But ok.", Priority.LOW);
        Warning w2 = new Warning("dummyFile.java", 0, "warningType", "-WWhatever", "Warning. Not ok!", Priority.LOW);
        Collection<FileAnnotation> warnings = new LinkedList<FileAnnotation>();
        warnings.add(w1);
        warnings.add(w2);

        WarningsFilter filter = new WarningsFilter();
        // exclude warnings with this warning message from the report
        final String excludeCategory = "-W#pragma-messages";
        warnings = filter.apply(warnings, null, null, null, excludeCategory, new NullLogger());

        assertFalse(warnings.contains(w1));
        assertTrue(warnings.contains(w2));
    }

    private Warning createDummyWarning(final String message) {
        return new Warning("dummyFile.java", 0, "warningType", "warningCategory", message, Priority.LOW);
    }

}

