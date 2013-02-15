package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.junit.Test;

/**
 * Tests the class {@link AntJavacParser} for output log of a HPI compile.
 */
public class HpiCompileParserTest extends ParserTester {
    private static final String WARNING_TYPE = Messages._Warnings_JavaParser_ParserName().toString(Locale.ENGLISH);

    /**
     * Parses a file with two deprecation warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseDeprecation() throws IOException {
        Collection<FileAnnotation> warnings = new AntJavacParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                46,
                "newInstance(org.kohsuke.stapler.StaplerRequest) in hudson.model.Descriptor has been deprecated",
                "C:/Build/Results/jobs/ADT-Base/workspace/tasks/src/main/java/hudson/plugins/tasks/TasksDescriptor.java",
                WARNING_TYPE, "Deprecation", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                34,
                "newInstance(org.kohsuke.stapler.StaplerRequest) in hudson.model.Descriptor has been deprecated",
                "C:/Build/Results/jobs/ADT-Base/workspace/tasks/src/main/java/hudson/plugins/tasks/TasksReporterDescriptor.java",
                WARNING_TYPE, "Deprecation", Priority.NORMAL);
    }

    @Override
    protected String getWarningsFile() {
        return "hpi.txt";
    }
}

