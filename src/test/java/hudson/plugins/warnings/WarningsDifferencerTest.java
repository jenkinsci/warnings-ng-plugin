package hudson.plugins.warnings;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import static org.junit.Assert.*;

import hudson.plugins.analysis.core.AnnotationDifferencer;
import hudson.plugins.analysis.test.AnnotationDifferencerTest;
import hudson.plugins.analysis.util.TreeString;
import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.parser.Gcc4CompilerParser;
import hudson.plugins.warnings.parser.Warning;

/**
 * Tests the {@link AnnotationDifferencer} for warnings.
 */
public class WarningsDifferencerTest extends AnnotationDifferencerTest {
    @Override
    public FileAnnotation createAnnotation(final String fileName, final Priority priority,
            final String message, final String category, final String type, final int start,
            final int end) {
        return new Warning(fileName, start, type, category, message, priority);
    }

    /**
     * Verifies that equals works with {@link TreeString} optimized path names.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-14821">Issue 14821</a>
     */
    @Test
    public void testIssue14821() {
        Collection<FileAnnotation> first = parse();
        Collection<FileAnnotation> second = parse();

        assertEquals("Wrong number of warnings", 16, first.size());
        assertEquals("Wrong number of warnings", 16, second.size());

        AbstractAnnotation.intern(first);

        Set<FileAnnotation> newAnnotations = AnnotationDifferencer.getNewAnnotations(ImmutableSet.copyOf(first), ImmutableSet.copyOf(second));
        assertEquals("Wrong number of new warnings", 0, newAnnotations.size());
        Set<FileAnnotation> fixedAnnotations = AnnotationDifferencer.getFixedAnnotations(ImmutableSet.copyOf(first), ImmutableSet.copyOf(second));
        assertEquals("Wrong number of fixed warnings", 0, fixedAnnotations.size());
    }

    private Collection<FileAnnotation> parse() {
        try {
            InputStream inputStream = WarningsDifferencerTest.class .getResourceAsStream("issue14821.txt");

            return new Gcc4CompilerParser().parse(new InputStreamReader(inputStream));
        }
        catch (IOException exception) {
            throw new IllegalArgumentException(exception);
        }
    }
}
