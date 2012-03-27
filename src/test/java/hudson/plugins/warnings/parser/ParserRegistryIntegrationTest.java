package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.FileAnnotation;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.localizer.Localizable;

import com.google.common.collect.Sets;

/**
 * Tests the class {@link ParserRegistry} in context of a running Jenkins instance.
 *
 * @author Ulli Hafner
 */
public class ParserRegistryIntegrationTest extends HudsonTestCase {
    /**
     * FIXME: Document field OLD_ID_O_JAVA_COMPILER
     */
    private static final String OLD_ID_JAVA_COMPILER = "Java Compiler";
    private static final String MIXED_API = "Both APIs";
    private static final String NEW_API = "New Parser API";
    private static final String OLD_API = "Old Parser API";

    /**
     * Verifies the current number of parsers. If you add a new parser then this
     * value needs to be adapted.
     */
    @Test
    public void testParserRegistration() {
        List<ParserDescription> groups = ParserRegistry.getAvailableParsers();

        assertEquals("Wrong number of registered parsers", 30, groups.size());
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
     * Verifies that we get the Java parser if we use the key of the 3.x
     * version.
     */
    @Test
    public void testOldJavaSerialization() {
        AbstractWarningsParser parser = ParserRegistry.getParser(OLD_ID_JAVA_COMPILER);

        verifyJavaParser(parser);
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
    public void testUiMapping() {
        boolean isFound = false;
        for (ParserDescription description : ParserRegistry.getAvailableParsers()) {
            if (description.isInGroup(OLD_ID_JAVA_COMPILER)) {
                verifyJavaParser(ParserRegistry.getParser(description.getGroup()));
                isFound = true;
            }
        }
        assertTrue("No parser found", isFound);
    }

    /**
     * Verifies that illegal names are filtered.
     */
    @Test
    public void testFiltering() {
        verifyFiltering(OLD_ID_JAVA_COMPILER);
        verifyFiltering(Messages._Warnings_JavaParser_ParserName().toString(Locale.ENGLISH));
    }

    private void verifyFiltering(final String validName) {
        List<String> filtered = ParserRegistry.filterExistingParserNames(Sets.newHashSet("Illegal", validName));

        assertEquals("Wrong number of filteres elements", 1, filtered.size());
        assertEquals("Wrong number of filteres elements", validName, filtered.get(0));
    }

    // CHECKSTYLE:OFF Test implementations
    @TestExtension
    public static class TestBothParser extends RegexpLineParser {
        private static final Localizable DUMMY = Messages._Warnings_NotLocalizedName(MIXED_API);
        private static final long serialVersionUID = 1L;

        public TestBothParser() {
            super(DUMMY, DUMMY, DUMMY, MIXED_API);
        }

        /** {@inheritDoc} */
        @Override
        protected Warning createWarning(final Matcher matcher) {
            return null;
        }

    }
    @TestExtension
    public static class TestNewParser extends AbstractWarningsParser {
        private static final long serialVersionUID = 1L;

        public TestNewParser() {
            super(NEW_API);
        }

        /** {@inheritDoc} */
        @Override
        public Collection<FileAnnotation> parse(final Reader reader) throws IOException,
                ParsingCanceledException {
            return null;
        }
    }
    @SuppressWarnings("deprecation")
    @TestExtension
    public static class TestOldParser implements WarningsParser {
        private static final long serialVersionUID = 1L;

        public Collection<FileAnnotation> parse(final Reader reader) throws IOException {
            return null;
        }

        /** {@inheritDoc} */
        public String getName() {
            return OLD_API;
        }
    }
}
