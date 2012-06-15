package hudson.plugins.analysis.util.model;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests the class {@link LineRangeList}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"PMD", "all"})
//CHECKSTYLE:OFF
public class LineRangeListTest {
    @Test
    public void bigValues() {
        LineRangeList r = new LineRangeList();
        LineRange v = new LineRange(1350, Integer.MAX_VALUE);
        r.add(v);
        assertTrue(r.contains(v));
    }

    @Test
    public void boundary() {
        LineRangeList r = new LineRangeList();
        LineRange v = new LineRange(128, 129);
        r.add(v);
        assertTrue(r.contains(v));
    }

    @Test
    public void basicCRUD() {
        LineRangeList r = new LineRangeList();
        LineRange v = new LineRange(1, 2);
        r.add(v);
        assertEquals(r.get(0), v);
        assertNotSame(r.get(0), v);
        assertEquals(1, r.size());

        LineRange v2 = new LineRange(3, 4);
        assertEquals(v, r.set(0, v2));
        assertEquals(r.get(0), v2);
        assertNotSame(r.get(0), v2);
        assertEquals(1, r.size());

        assertEquals(v2, r.remove(0));
        assertEquals(0, r.size());
    }

    /**
     * Tests the internal buffer resize operation.
     */
    @Test
    public void resize() {
        LineRangeList r = new LineRangeList();
        for (int i = 0; i < 100; i++) {
            r.add(new LineRange(i * 2, i * 2 + 1));
        }
        r.trim();
        assertEquals(100, r.size());

        for (int i = 0; i < 100; i++) {
            assertEquals(new LineRange(i * 2, i * 2 + 1), r.get(i));
        }

        assertEquals(100, r.size());
    }

    @Test
    public void contains() {
        LineRangeList r = new LineRangeList();
        r.add(new LineRange(0, 1));
        r.add(new LineRange(2, 3));
        r.add(new LineRange(4, 5));

        r.remove(new LineRange(4, 5));

        assertTrue(r.contains(new LineRange(0, 1)));
        assertTrue(r.contains(new LineRange(2, 3)));
        assertFalse(r.contains(new LineRange(4, 5)));
    }
}