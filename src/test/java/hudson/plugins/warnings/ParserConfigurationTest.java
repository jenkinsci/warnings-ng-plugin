package hudson.plugins.warnings;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.google.common.collect.Lists;

import static org.junit.Assert.*;

import hudson.plugins.warnings.parser.JavacParser;

/**
 * Tests the class {@link ParserConfiguration}.
 *
 * @author Ullrich Hafner
 */
public class ParserConfigurationTest {
    private static final String PATTERN = "**/*.java";

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    /**
     * Verifies that filtering of non-existing parsers works.
     */
    @Test
    public void testFiltering() {
        ParserConfiguration javaParser = new ParserConfiguration(PATTERN, new JavacParser().getGroup());
        ParserConfiguration unknownParser = new ParserConfiguration(PATTERN, "unknown");

        assertArrayEquals("Wrong filtering: ", new ParserConfiguration[] {javaParser},
                ParserConfiguration.filterExisting(Lists.newArrayList(javaParser, unknownParser)));
        assertArrayEquals("Wrong filtering: ", new ParserConfiguration[] {},
                ParserConfiguration.filterExisting(Lists.newArrayList(unknownParser)));
        assertArrayEquals("Wrong filtering: ", new ParserConfiguration[] {javaParser},
                ParserConfiguration.filterExisting(Lists.newArrayList(javaParser)));
    }
}

