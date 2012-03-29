package hudson.plugins.analysis.util;

import static junit.framework.Assert.*;

import org.junit.Test;

/**
 * Tests the class {@link HtmlPrinter}.
 *
 * @author Ulli Hafner
 */
public class HtmlPrinterTest {
    private static final String DUMMY_LINK = "http://link.de";
    private static final String DUMMY_TEXT = "Text";

    /**
     * Test creation of li.
     */
    @Test
    public void testItem() {
        HtmlPrinter printer = new HtmlPrinter();

        assertEquals("Wrong list item output", "<li>Text</li>", printer.item(DUMMY_TEXT));
    }

    /**
     * Test creation of href.
     */
    @Test
    public void testHRefItem() {
        HtmlPrinter printer = new HtmlPrinter();

        assertEquals("Wrong list item output", "<a href=\"http://link.de\">Text</a>", printer.link(DUMMY_LINK, DUMMY_TEXT));
    }

    /**
     * Test recursion.
     */
    @Test
    public void testComplex() {
        HtmlPrinter printer = new HtmlPrinter();

        assertEquals("Wrong list item output", "<li><a href=\"http://link.de\">Text</a></li>", printer.item(printer.link(DUMMY_LINK, DUMMY_TEXT)));
    }

    /**
     * Tests that we can store the text in a buffer.
     */
    @Test
    public void testBuffer() {
        HtmlPrinter printer = new HtmlPrinter();
        printer.append(DUMMY_TEXT);
        printer.append(printer.item(printer.link(DUMMY_LINK, DUMMY_TEXT)));

        assertEquals("Wrong list item output", "Text<li><a href=\"http://link.de\">Text</a></li>", printer.toString());
    }
}

