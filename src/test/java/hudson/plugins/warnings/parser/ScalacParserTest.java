package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import hudson.plugins.analysis.util.model.Priority;
import org.junit.Test;
import static org.junit.Assert.*;

import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Tests the class {@link ScalacParser}.
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 */
public class ScalacParserTest extends ParserTester {

    @Test
    public void basicFunctionality() throws IOException {
        Collection<FileAnnotation> warnings = parse("scalac.txt");
        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());
        Iterator<FileAnnotation> iter = warnings.iterator();
        checkWarning(iter.next(), 29, "implicit conversion method toLab2OI should be enabled",
                "/home/user/.jenkins/jobs/job/workspace/some/path/SomeFile.scala", "warning", Priority.NORMAL);
        checkWarning(iter.next(), 408, "method asJavaMap in object JavaConversions is deprecated: use mapAsJavaMap instead",
                "/home/user/.jenkins/jobs/job/workspace/another/path/SomeFile.scala", "warning", Priority.NORMAL);
        checkWarning(iter.next(), 59, "method error in object Predef is deprecated: Use `sys.error(message)` instead",
                "/home/user/.jenkins/jobs/job/workspace/yet/another/path/SomeFile.scala", "warning", Priority.HIGH);
    }

    private Collection<FileAnnotation> parse(final String fileName) throws IOException {
        return new ScalacParser().parse(openFile(fileName));
    }

    @Override
    protected String getWarningsFile() {
        return "scalac.txt";
    }
}
