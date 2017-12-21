package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.AbstractParserTest;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.assertj.SoftAssertions;
import edu.hm.hafner.analysis.parser.AjcParser;
import edu.hm.hafner.analysis.parser.AnsibleLintParser;
import hudson.console.ConsoleNote;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

/**
 * Tests the class {@link Eclipse}.
 *
 * @author Ullrich Hafner
 */
class AjcTest extends AbstractParserTest {
    AjcTest() {
        super("ajc.txt");
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
        assertThat(issues).hasSize(9);

        for (Issue annotation : issues) {
            assertThatMessageContainsWord(annotation);
        }
    }

    @Override
    protected AbstractParser createParser() {
        AjcParser parser = new AjcParser();
        parser.setTransformer(ConsoleNote::removeNotes);
        return parser;
    }
}