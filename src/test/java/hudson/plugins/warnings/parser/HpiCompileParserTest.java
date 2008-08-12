package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link HpiCompileParser}.
 */
public class HpiCompileParserTest extends ParserTester {
    /**
     * Parses a file with two deprecation warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseDeprecation() throws IOException {
        Collection<FileAnnotation> warnings = new HpiCompileParser().parse(HpiCompileParserTest.class.getResourceAsStream("hpi.txt"));

        assertEquals("Wrong number of warnings detected.", 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                46,
                "newInstance(org.kohsuke.stapler.StaplerRequest) in hudson.model.Descriptor has been deprecated",
                "C:/Build/Results/jobs/ADT-Base/workspace/tasks/src/main/java/hudson/plugins/tasks/TasksDescriptor.java",
                HpiCompileParser.WARNING_TYPE, "deprecation", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                34,
                "newInstance(org.kohsuke.stapler.StaplerRequest) in hudson.model.Descriptor has been deprecated",
                "C:/Build/Results/jobs/ADT-Base/workspace/tasks/src/main/java/hudson/plugins/tasks/TasksReporterDescriptor.java",
                HpiCompileParser.WARNING_TYPE, "deprecation", Priority.NORMAL);
    }

    /**
     * Parses a warning log with 2 ANT warnings that should produce no HPI warning.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="https://hudson.dev.java.net/issues/show_bug.cgi?id=2133">Issue 2133</a>
     */
    @Test
    public void issue2133() throws IOException {
        Collection<FileAnnotation> warnings = new HpiCompileParser().parse(HpiCompileParserTest.class.getResourceAsStream("issue2133.txt"));

        assertEquals("Wrong number of warnings detected.", 0, warnings.size());
    }
}

