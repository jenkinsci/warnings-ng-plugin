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
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.localizer.Localizable;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import static org.junit.Assert.*;

import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.warnings.GroovyParser;
import hudson.plugins.warnings.GroovyParserTest;

/**
 * Tests the class {@link ParserRegistry} in context of a running Jenkins instance.
 *
 * @author Ulli Hafner
 */
public class ParserRegistryIntegrationTest {
    /** If you add a new parser then this value needs to be adapted. */
    private static final int NUMBER_OF_AVAILABLE_PARSERS = 68;
    private static final String OLD_ID_ECLIPSE_JAVA_COMPILER = "Eclipse Java Compiler";
    private static final String JAVA_WARNINGS_FILE = "deprecations.txt";
    private static final String OLD_ID_JAVA_COMPILER = "Java Compiler";
    private static final String MIXED_API = "Both APIs";
    private static final String NEW_API = "New Parser API";
    private static final String OLD_API = "Old Parser API";

    @ClassRule
    public static JenkinsRule jenkins = new JenkinsRule();

    /**
     * Tests the construction of dynamic parsers.
     */
    @Test
    public void testDynamicParsers() {
        GroovyParser multi = new GroovyParser("name", GroovyParserTest.MULTI_LINE_REGEXP, "empty");
        GroovyParser single = new GroovyParser("name", GroovyParserTest.SINGLE_LINE_REGEXP, "empty");

        List<AbstractWarningsParser> allParsers = ParserRegistry.getDynamicParsers(Lists.newArrayList(single, multi));

        int multiNumber = 0;
        int singleNumber = 0;

        for (AbstractWarningsParser parser : allParsers) {
            if (parser.getClass() == DynamicParser.class) {
                singleNumber++;
            }
            else if (parser.getClass() == DynamicDocumentParser.class) {
                multiNumber++;
            }
        }

        assertEquals("Wrong number of single line parsers" , 1, singleNumber);
        assertEquals("Wrong number of multi line parsers" , 1, multiNumber);
    }

    /**
     * Parses a warning log with 7 warnings, 2 have no category.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-38557">Issue 38557</a>
     */
    @Test
    public void issue38557() throws IOException {
        Collection<FileAnnotation> warnings = parse("issue38557.txt");

        verifyNumberOfWarnings(warnings, 7);

        DefaultAnnotationContainer container = new DefaultAnnotationContainer();
        container.addAnnotations(warnings);
        verifyNumberOfWarnings(container.getAnnotations(), 7);

        int count = 0;
        for (AnnotationContainer category : container.getCategories()) {
            count += category.getNumberOfAnnotations();
        }
        verifyNumberOfWarnings(7, count);
    }

    private void verifyNumberOfWarnings(final Collection<FileAnnotation> warnings, final int expected) {
        verifyNumberOfWarnings(expected, warnings.size());
    }

    private void verifyNumberOfWarnings(final int expected, final int actual) {
        assertEquals(String.format("There should be %d warnings", expected), expected, actual);
    }

    /**
     * Parses a warning log with two warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-24611">Issue 24611</a>
     */
    @Test
    public void testIssue24611() throws IOException {
        Collection<FileAnnotation> warnings = parse("issue24611.txt");

        verifyNumberOfWarnings(warnings, 2);
    }

    private Collection<FileAnnotation> parse(final String fileName) throws IOException {
        InputStream file = ParserRegistryIntegrationTest.class.getResourceAsStream(fileName);
        ParserRegistry registry = new ParserRegistry(ParserRegistry.getParsers("Java Compiler (javac)"), null);
        String text = IOUtils.toString(file);
        return registry.parse(new ReaderInputStream(new StringReader(text)));
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
    @SuppressFBWarnings("SIC")
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
