package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.AbstractParserTest;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.assertj.SoftAssertions;
import edu.hm.hafner.analysis.parser.AcuCobolParser;
import edu.hm.hafner.analysis.parser.EclipseParser;
import hudson.console.ConsoleNote;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

/**
 * Tests the class {@link AcuCobol}.
 *
 * @author Ullrich Hafner
 */
class AcuCobolTest extends AbstractParserTest {
    AcuCobolTest() {
        super("acu.txt");
    }

    private void assertThatMessageContainsWord(final Issue annotation) {
        assertThat(annotation.getMessage()).matches("[a-zA-Z].*");
    }

    /**
     * Parses a warning log with console annotations which are removed.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-11675">Issue 11675</a>
     */
    @Override
    protected void assertThatIssuesArePresent(final Issues<Issue> issues, final SoftAssertions softly) {
        assertThat(issues).hasSize(4);

        for (Issue annotation : issues) {
            assertThatMessageContainsWord(annotation);
        }
    }

    @Override
    protected AbstractParser createParser() {
        AcuCobolParser parser = new AcuCobolParser();
        parser.setTransformer(ConsoleNote::removeNotes);
        return parser;
    }
}