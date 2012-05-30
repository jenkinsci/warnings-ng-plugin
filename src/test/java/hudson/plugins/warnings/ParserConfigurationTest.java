package hudson.plugins.warnings;

import static org.junit.Assert.*;
import hudson.plugins.warnings.parser.JavacParser;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import com.google.common.collect.Lists;

/**
 * Tests the class {@link ParserConfiguration}.
 *
 * @author Ulli Hafner
 */
public class ParserConfigurationTest extends HudsonTestCase {
    private static final String PATTERN = "**/*.java";

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

