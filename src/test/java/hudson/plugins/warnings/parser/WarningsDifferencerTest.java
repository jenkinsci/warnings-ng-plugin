package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.AnnotationDifferencer;
import hudson.plugins.warnings.util.AnnotationDifferencerTest;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

/**
 * Tests the {@link AnnotationDifferencer} for warnings.
 */
public class WarningsDifferencerTest extends AnnotationDifferencerTest {
    /** {@inheritDoc} */
    @Override
    public FileAnnotation createAnnotation(final String fileName, final Priority priority, final String message, final String category,
            final String type, final int start, final int end) {
        return new Warning(fileName, start, type, category, message, priority);
    }
}

