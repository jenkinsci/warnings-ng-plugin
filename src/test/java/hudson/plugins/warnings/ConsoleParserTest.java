package hudson.plugins.warnings;

import static org.junit.Assert.*;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import com.google.common.collect.Lists;

import hudson.plugins.warnings.parser.JavacParser;

/**
 * Tests the class  {@link ConsoleParser}.
 *
 * @author Ulli Hafner
 */
public class ConsoleParserTest extends HudsonTestCase {
    /**
     * Verifies that filtering of non-existing parsers works.
     */
    @Test
    public void testFiltering() {
        ConsoleParser javaParser = new ConsoleParser(new JavacParser().getGroup());
        ConsoleParser unknownParser = new ConsoleParser("unknown");

        assertArrayEquals("Wrong filtering: ", new ConsoleParser[] {javaParser},
                ConsoleParser.filterExisting(Lists.newArrayList(javaParser, unknownParser)));
        assertArrayEquals("Wrong filtering: ", new ConsoleParser[] {},
                ConsoleParser.filterExisting(Lists.newArrayList(unknownParser)));
        assertArrayEquals("Wrong filtering: ", new ConsoleParser[] {javaParser},
                ConsoleParser.filterExisting(Lists.newArrayList(javaParser)));
    }
}

