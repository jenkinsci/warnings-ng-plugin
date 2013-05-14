package hudson.plugins.warnings;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import com.google.common.collect.Lists;

import hudson.model.Action;

/**
 * Tests the class {@link WarningsPublisher}.
 *
 * @author Ulli Hafner
 */
public class WarningsPublisherTest extends HudsonTestCase {
    private static final String SUFFIX_NAME = " Warnings";
    private static final String SECOND = "JSLint";
    private static final String FIRST = "Maven";
    private static final String PATTERN = "Pattern";

    /**
     * Verifies that the order of warnings is preserved.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-14615">Issue 14615</a>
     */
    @Test
    public void testIssue14615Console() {
        List<ConsoleParser> consoleParsers = Lists.newArrayList();
        consoleParsers.add(new ConsoleParser(FIRST));
        consoleParsers.add(new ConsoleParser(SECOND));

        List<String> expected = createExpectedResult();

        checkConsoleOrder(consoleParsers, expected);

        Collections.reverse(consoleParsers);
        Collections.reverse(expected);

        checkConsoleOrder(consoleParsers, expected);
    }

    /**
     * Verifies that the order of warnings is preserved.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-14615">Issue 14615</a>
     */
    @Test
    public void testIssue14615File() {
        List<ParserConfiguration> fileParsers = Lists.newArrayList();
        fileParsers.add(new ParserConfiguration(PATTERN, FIRST));
        fileParsers.add(new ParserConfiguration(PATTERN, SECOND));

        List<String> expected = createExpectedResult();

        checkFileOrder(fileParsers, expected);

        Collections.reverse(fileParsers);
        Collections.reverse(expected);

        checkFileOrder(fileParsers, expected);
    }

    private List<String> createExpectedResult() {
        List<String> expected = Lists.newArrayList();
        expected.add(FIRST + SUFFIX_NAME);
        expected.add(SECOND + SUFFIX_NAME);
        return expected;
    }

    private void checkFileOrder(final List<ParserConfiguration> fileParsers, final List<String> expected) {
        WarningsPublisher publisher = new WarningsPublisher(null, null, null, null, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, null, null, false,
                fileParsers, null);
        checkOrder(expected, publisher);
    }

    private void checkConsoleOrder(final List<ConsoleParser> consoleParsers, final List<String> expected) {
        WarningsPublisher publisher = new WarningsPublisher(null, null, null, null, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, null, null, false,
                null, consoleParsers);
        checkOrder(expected, publisher);
    }

    private void checkOrder(final List<String> expected, final WarningsPublisher publisher) {
        List<Action> ordered = Lists.newArrayList(publisher.getProjectActions(null));

        assertEquals("Wrong number of actions.", 2, ordered.size());

        for (int position = 0; position < expected.size(); position++) {
            assertPosition(ordered, expected, position);
        }
    }

    private void assertPosition(final List<Action> ordered, final List<String> expected, final int position) {
        assertEquals("Wrong action at position " + position, expected.get(position), ordered.get(position).getDisplayName());
    }
}

