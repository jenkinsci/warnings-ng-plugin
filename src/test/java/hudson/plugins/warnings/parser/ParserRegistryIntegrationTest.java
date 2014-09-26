package hudson.plugins.warnings.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.localizer.Localizable;

import static org.junit.Assert.*;

import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Tests the class {@link ParserRegistry} in context of a running Jenkins instance.
 *
 * @author Ulli Hafner
 */
public class ParserRegistryIntegrationTest {
    /** If you add a new parser then this value needs to be adapted. */
    private static final int NUMBER_OF_AVAILABLE_PARSERS = 56;
    private static final String OLD_ID_ECLIPSE_JAVA_COMPILER = "Eclipse Java Compiler";
    private static final String JAVA_WARNINGS_FILE = "deprecations.txt";
    private static final String OLD_ID_JAVA_COMPILER = "Java Compiler";
    private static final String MIXED_API = "Both APIs";
    private static final String NEW_API = "New Parser API";
    private static final String OLD_API = "Old Parser API";

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    /**
     * Parses a warning log with two warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-24611">Issue 24611</a>
     */
    @Test
    public void testIssue24611() throws IOException {
        InputStream file = ParserRegistryIntegrationTest.class.getResourceAsStream("issue24611.txt");
        ParserRegistry registry = new ParserRegistry(ParserRegistry.getParsers("Java Compiler (javac)"), null);
        String text = IOUtils.toString(file);
        Set<FileAnnotation> warnings = registry.parse(new ReaderInputStream(new StringReader(text)));

        assertEquals("There should be 2 warnings", 2, warnings.size());
    }

    /**
     * Verifies the current number of parsers.
     */
    @Test
    public void testParserRegistration() {
        List<ParserDescription> groups = ParserRegistry.getAvailableParsers();

        int expected;
        if (PluginDescriptor.isPluginInstalled("violations")) {
            expected = NUMBER_OF_AVAILABLE_PARSERS;
        }
        else {
            expected = NUMBER_OF_AVAILABLE_PARSERS - 1;
        }
        assertEquals("Wrong number of registered parsers", expected, groups.size());
    }

    /**
     * Verifies that the registry detects old and new API extensions and maps them correctly.
     */
    @Test
    public void testRegistry() {
        assertEquals("Wrong new API implementations", 1, ParserRegistry.getParsers(NEW_API).size());
        assertEquals("Wrong old API implementations", 1, ParserRegistry.getParsers(OLD_API).size());
        assertEquals("Wrong mixed API implementations", 1, ParserRegistry.getParsers(MIXED_API).size());
    }

    /**
     * Verifies that the registry detects old and new API extensions and maps them correctly.
     */
    @Test
    public void testIssue17762() {
        verifyRegistryWithOldAndNewName("Apple LLVM Compiler (Clang)", "Clang (LLVM based)", 1);
    }

    private void verifyRegistryWithOldAndNewName(final String oldId, final String newId, final int expectedNumberOfParsers) {
        assertEquals("Wrong old API implementations", expectedNumberOfParsers, ParserRegistry.getParsers(oldId).size());
        assertEquals("Wrong new API implementations", expectedNumberOfParsers, ParserRegistry.getParsers(newId).size());

        assertTrue("Parser does not exist: " + oldId, ParserRegistry.exists(oldId));
        assertTrue("Parser does not exist: " + newId, ParserRegistry.exists(newId));
    }

    /**
     * Verifies that the registry detects old and new API extensions and maps them correctly.
     */
    @Test
    public void testIssue20545() {
        verifyRegistryWithOldAndNewName("GNU Compiler 4 (gcc)", "GNU C Compiler 4 (gcc)", 2);
    }

    /**
     * Verifies that the registry detects old and new API extensions and maps them correctly.
     */
    @Test
    public void testIssue20658() {
        verifyRegistryWithOldAndNewName("GNU Make + GNU Compiler (gcc)", "GNU Make + GNU C Compiler (gcc)", 1);
    }

    /**
     * Verifies that the registry detects old and new API extensions and maps them correctly.
     */
    @Test
    public void testPullRequest41() {
        verifyRegistryWithOldAndNewName("Reshaper InspectCode", "Resharper InspectCode", 1);
    }

    /**
     * Verifies that we parse two warnings if we use the key of the 3.x version.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testOldJavaSerializationActualParsing() throws IOException {
        ParserRegistry registry = createRegistryUnderTest(JAVA_WARNINGS_FILE, OLD_ID_JAVA_COMPILER);
        Collection<FileAnnotation> annotations = registry.parse(new File(JAVA_WARNINGS_FILE));

        assertEquals("Wrong number of warnings parsed.", 3, annotations.size());
    }

    /**
     * Verifies that we get the Java Eclipse parser if we use the key of the 3.x
     * version.
     */
    @Test
    public void testOldEclipseSerialization() {
        verifyEclipseParser(ParserRegistry.getParser(OLD_ID_ECLIPSE_JAVA_COMPILER));
    }

    private void verifyEclipseParser(final AbstractWarningsParser parser) {
        assertEquals("Wrong name",
                Messages._Warnings_EclipseParser_ParserName().toString(), parser.getParserName().toString());
        assertEquals("Wrong link",
                Messages._Warnings_EclipseParser_LinkName().toString(), parser.getLinkName().toString());
        assertEquals("Wrong trend",
                Messages._Warnings_EclipseParser_TrendName().toString(), parser.getTrendName().toString());
    }

    /**
     * Verifies that we get the Java parser if we use the key of the 3.x
     * version.
     */
    @Test
    public void testOldJavaSerialization() {
        verifyJavaParser(ParserRegistry.getParser(OLD_ID_JAVA_COMPILER));
    }

    private void verifyJavaParser(final AbstractWarningsParser parser) {
        assertEquals("Wrong name",
                Messages._Warnings_JavaParser_ParserName().toString(), parser.getParserName().toString());
        assertEquals("Wrong link",
                Messages._Warnings_JavaParser_LinkName().toString(), parser.getLinkName().toString());
        assertEquals("Wrong trend",
                Messages._Warnings_JavaParser_TrendName().toString(), parser.getTrendName().toString());
    }

    /**
     * Verifies that we get the Java parser (using
     * {@link ParserRegistry#getAvailableParsers()} and
     * {@link ParserDescription}) if we use the key of the 3.x version.
     */
    @Test
    public void testUiMappingJava() {
        ParserDescription description = verifyThatParserExists(OLD_ID_JAVA_COMPILER);
        verifyJavaParser(ParserRegistry.getParser(description.getGroup()));
    }

    /**
     * Verifies that we get the Eclipse parser (using
     * {@link ParserRegistry#getAvailableParsers()} and
     * {@link ParserDescription}) if we use the key of the 3.x version.
     */
    @Test
    public void testUiMappingEclipse() {
        ParserDescription description = verifyThatParserExists(OLD_ID_ECLIPSE_JAVA_COMPILER);
        verifyEclipseParser(ParserRegistry.getParser(description.getGroup()));
    }

    private ParserDescription verifyThatParserExists(final String parserName) {
        for (ParserDescription description : ParserRegistry.getAvailableParsers()) {
            if (description.isInGroup(parserName)) {
                return description;
            }
        }
        fail("No parser found for ID: " + parserName);
        return null;
    }

    /**
     * Creates the registry under test.
     *
     * @param fileName
     *            file name with the warnings
     * @param group
     *            the parsers to use
     * @return the registry
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SIC")
    private ParserRegistry createRegistryUnderTest(final String fileName, final String group) {
        ParserRegistry parserRegistry = new ParserRegistry(ParserRegistry.getParsers(group), "") {
                    @Override
            protected Reader createReader(final File file) throws FileNotFoundException {
                return new InputStreamReader(ParserRegistryTest.class.getResourceAsStream(fileName));
            }
        };
        return parserRegistry;
    }

    // CHECKSTYLE:OFF Test implementations
    @SuppressWarnings("javadoc")
    @TestExtension
    public static class TestBothParser extends RegexpLineParser {
        private static final Localizable DUMMY = Messages._Warnings_NotLocalizedName(MIXED_API);
        private static final long serialVersionUID = 1L;

        public TestBothParser() {
            super(DUMMY, DUMMY, DUMMY, MIXED_API);
        }

            @Override
        protected Warning createWarning(final Matcher matcher) {
            return null;
        }

    }
    @SuppressWarnings("javadoc")
    @TestExtension
    public static class TestNewParser extends AbstractWarningsParser {
        private static final long serialVersionUID = 1L;

        public TestNewParser() {
            super(NEW_API);
        }

            @Override
        public Collection<FileAnnotation> parse(final Reader reader) throws IOException,
                ParsingCanceledException {
            return null;
        }
    }
    @SuppressWarnings({"javadoc", "deprecation"})
    @TestExtension
    public static class TestOldParser implements WarningsParser {
        private static final long serialVersionUID = 1L;

        @Override
        public Collection<FileAnnotation> parse(final Reader reader) throws IOException {
            return null;
        }

            @Override
        public String getName() {
            return OLD_API;
        }
    }
}
