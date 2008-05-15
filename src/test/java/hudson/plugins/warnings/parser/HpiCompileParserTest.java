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
}

