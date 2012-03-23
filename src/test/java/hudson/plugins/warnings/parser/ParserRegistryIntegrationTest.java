package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.FileAnnotation;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.regex.Matcher;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.localizer.Localizable;

/**
 * Tests the class {@link ParserRegistry} in context of a running Jenkins instance.
 *
 * @author Ulli Hafner
 */
public class ParserRegistryIntegrationTest extends HudsonTestCase {
    private static final String MIXED_API = "Both APIs";
    private static final String NEW_API = "New Parser API";
    private static final String OLD_API = "Old Parser API";

    /**
     * Verifies that the registry detects old and new API extensions and maps them correctly.
     */
    @Test
    public void testRegistry() {
        assertEquals("Wrong new API implementations", 1, ParserRegistry.getParsers(NEW_API).size());
        assertEquals("Wrong old API implementations", 1, ParserRegistry.getParsers(OLD_API).size());
        assertEquals("Wrong mixed API implementations", 1, ParserRegistry.getParsers(MIXED_API).size());
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
