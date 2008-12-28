package hudson.plugins.warnings.util;

import static org.easymock.EasyMock.*;
import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.LineRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Assert;
import org.junit.Test;

/**
 *  Tests the class {@link SourceDetail}.
 */
public class SourceDetailTest {
    /** Start of the range. */
    private static final int START = 6;
    /** Reference to line 6. */
    private static final String LINE_6_INDICATOR = "<a name=\"" + START + "\">";

    /**
     * Checks whether we correctly find a specific line in the generated source
     * code at a fixed line offset.
     *
     * @throws IOException in case of an IO error
     */
    @Test
    public void checkCorrectOffset() throws IOException {
        FileAnnotation annotation = createMock(FileAnnotation.class);

        expect(annotation.getFileName()).andReturn("").anyTimes();
        expect(annotation.getTempName((AbstractBuild<?, ?>)anyObject())).andReturn("").anyTimes();

        replay(annotation);

        SourceDetail source = new SourceDetail(null, annotation, null);

        InputStream stream = SourceDetailTest.class.getResourceAsStream("AbortException.txt");
        String highlighted = source.highlightSource(stream);

        LineIterator lineIterator = IOUtils.lineIterator(new StringReader(highlighted));

        int line = 1;
        int offset = 1;
        while (lineIterator.hasNext()) {
            String content = lineIterator.nextLine();
            if (content.contains(LINE_6_INDICATOR)) {
                offset  = line - 6;
            }
            line++;
        }
        Assert.assertEquals("Wrong offset during source highlighting.", 12, offset);

        verify(annotation);
    }

    /**
     * Checks whether we correctly highlight the source for a single line.
     *
     * @throws IOException in case of an IO error
     */
    @Test
    public void splitSingleLine() throws IOException {
        ArrayList<LineRange> lineRanges = new ArrayList<LineRange>();
        lineRanges.add(new LineRange(6));
        split("ExpectedRendering-Line6.html", lineRanges);
    }

    /**
     * Checks whether we correctly highlight the source for range of 7 lines.
     *
     * @throws IOException in case of an IO error
     */
    @Test
    public void splitLineRange() throws IOException {
        ArrayList<LineRange> lineRanges = new ArrayList<LineRange>();
        lineRanges.add(new LineRange(6, 12));
        split("ExpectedRendering-Line6-12.html", lineRanges);
    }

    /**
     * Checks whether we correctly highlight the source for 2 ranges.
     *
     * @throws IOException in case of an IO error
     */
    @Test
    public void splitTwoRanges() throws IOException {
        ArrayList<LineRange> lineRanges = new ArrayList<LineRange>();
        lineRanges.add(new LineRange(1, 4));
        lineRanges.add(new LineRange(14, 20));
        split("ExpectedRendering-2-Ranges.html", lineRanges);
    }

    /**
     * Checks whether we correctly split the source into prefix, warning and
     * suffix.
     *
     * @param fileName
     *            the filename of the expected result
     * @param lineRanges
     *            the ranges to test
     * @throws IOException
     *             in case of an IO error
     */
    @SuppressWarnings("unchecked")
    private void split(final String fileName, final List<LineRange> lineRanges) throws IOException {
        InputStream stream = SourceDetailTest.class.getResourceAsStream("AbortException.txt");

        FileAnnotation annotation = createMock(FileAnnotation.class);

        expect(annotation.getLineRanges()).andReturn(lineRanges);
        expect(annotation.getFileName()).andReturn("").anyTimes();
        expect(annotation.getTempName((AbstractBuild<?, ?>)anyObject())).andReturn("").anyTimes();
        expect(annotation.getMessage()).andReturn("Message ").anyTimes();
        expect(annotation.getToolTip()).andReturn("Tooltip").anyTimes();

        replay(annotation);

        SourceDetail source = new SourceDetail(null, annotation, null);

        String highlighted = source.highlightSource(stream);
        source.splitSourceFile(highlighted);

        List<String> expected = IOUtils.readLines(SourceDetailTest.class.getResourceAsStream(fileName));
        List<String> actual = IOUtils.readLines(new StringReader(source.getSourceCode()));

        Iterator<String> expectedIterator = expected.iterator();
        Iterator<String> actualIterator = actual.iterator();
        while (actualIterator.hasNext()) {
            String expectedLine = expectedIterator.next().trim();
            String actualLine = actualIterator.next().trim();

            Assert.assertEquals(expectedLine, actualLine);
        }

        verify(annotation);
    }
}

