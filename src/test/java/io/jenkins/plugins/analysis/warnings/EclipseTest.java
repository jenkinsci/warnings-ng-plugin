package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.AbstractParserTest;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import static edu.hm.hafner.analysis.assertj.Assertions.*;
import edu.hm.hafner.analysis.assertj.SoftAssertions;
import edu.hm.hafner.analysis.parser.EclipseParser;

import hudson.console.ConsoleNote;

/**
 * Tests the class {@link Eclipse}.
 *
 * @author Ullrich Hafner
 */
class EclipseTest extends AbstractParserTest {
    public EclipseTest() {
        super("issue11675.txt");
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
        assertThat(issues).hasSize(8);

        for (Issue annotation : issues) {
            assertThatMessageContainsWord(annotation);
        }
    }

    @Override
    protected AbstractParser createParser() {
        EclipseParser parser = new EclipseParser();
        parser.setTransformer(line -> ConsoleNote.removeNotes(line));
        return parser;
    }
}