package hudson.plugins.analysis.util.model;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

import hudson.util.RobustCollectionConverter;

/**
 * {@link List} of {@link LineRange} that stores values more efficiently at
 * runtime.
 * <p>
 * This class thinks of {@link LineRange} as two integers (start and end-start),
 * hence a list of {@link LineRange} becomes a list of integers. The class then
 * stores those integers in {@code byte[]}. Each number is packed to UTF-8 like
 * variable length format. To store a long value N, we first split into 7 bit
 * chunk, and store each 7 bit chunk as a byte, in the little endian order. The
 * last byte gets its 8th bit set to indicate that that's the last byte. Thus in
 * this format, 0x0 gets stored as 0x80, 0x1234 gets stored as
 * {0x34,0xA4(0x24|0x80)}.
 * <p>
 * This variable length mode stores data most efficiently, since most line
 * numbers are small. Access characteristic gets close to that of
 * {@link LinkedList}, since we can only traverse this packed byte[] from the
 * start or from the end.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"PMD", "all"})
// CHECKSTYLE:OFF
public class LineRangeList extends AbstractList<LineRange> {
    /**
     * Encoded bits.
     */
    private byte[] data;
    /**
     * Number of bytes in {@link #data} that's already used. This is not
     * {@link List#size()}.
     */
    private int len;

    public LineRangeList() {
        this(16);
    }

    public LineRangeList(final int capacity) {
        data = new byte[capacity];
        len = 0;
    }

    public LineRangeList(final Collection<LineRange> copy) {
        data = new byte[copy.size() * 4]; // guess
        for (LineRange lr : copy) {
            add(lr);
        }
    }

    private class Cursor implements ListIterator<LineRange> {
        int pos = 0;

        private Cursor(final int pos) {
            this.pos = pos;
        }

        private Cursor() {
        }

        /**
         * Does the opposite of {@link #read()} and skips back one int.
         */
        private void prev() {
            if (pos == 0) {
                throw new IllegalArgumentException();
            }
            do {
                pos--;
            } while (pos > 0 && (data[pos - 1] & 0x80) == 0);
        }

        /**
         * Reads the {@link LineRange} object the cursor is pointing at.
         */
        public LineRange next() {
            int s = read();
            int d = read();
            return new LineRange(s, s + d);
        }

        public LineRange previous() {
            prev();
            prev();
            return copy().next();
        }

        /**
         * Removes the last returned value.
         */
        public void remove() {
            prev();
            prev();
            delete();
        }

        public boolean hasNext() {
            return pos < len;
        }

        public boolean hasPrevious() {
            return pos > 0;
        }

        /**
         * Reads the current variable-length encoded int value under the cursor,
         * and moves the cursor ahead.
         */
        private int read() {
            if (len <= pos) {
                throw new IndexOutOfBoundsException();
            }

            int i = 0;
            int v = 0;
            do {
                v += (data[pos] & 0x7F) << ((i++) * 7);
            } while ((data[pos++] & 0x80) == 0);
            return v;
        }

        private void write(int i) {
            boolean last;
            do {
                last = i < 0x80;
                data[pos++] = (byte)((i & 0x7F) | (last ? 0x80 : 0));
                i /= 0x80;
            } while (!last);
        }

        private void write(final LineRange r) {
            write(r.getStart());
            write(r.getEnd() - r.getStart());
        }

        /**
         * Reads the current value at the cursor and compares it.
         */
        public boolean compare(final LineRange lr) {
            int s = read();
            int d = read();
            return lr.getStart() == s && lr.getEnd() == s + d;
        }

        /**
         * Skips forward and gets the pointer to N-th element.
         */
        private Cursor skip(int n) {
            for (; n > 0; n--) {
                read();
                read();
            }
            return this;
        }

        /**
         * Counts the # of elements from the current cursor position to the end.
         */
        private int count() {
            int n = 0;
            while (pos < len) {
                read();
                read();
                n++;
            }
            return n;
        }

        public int nextIndex() {
            throw new UnsupportedOperationException();
        }

        public int previousIndex() {
            throw new UnsupportedOperationException();
        }

        public Cursor copy() {
            return new Cursor(pos);
        }

        private void adjust(final int diff) {
            ensure(len + diff);
            if (diff > 0) {
                System.arraycopy(data, pos, data, pos + diff, len - pos);
            }
            else {
                System.arraycopy(data, pos - diff, data, pos, len - pos + diff);
            }
            len += diff;
        }

        /**
         * Rewrites the value at the current cursor position.
         */
        public LineRange _set(final LineRange v) {
            Cursor c = copy();
            LineRange old = c.next();
            int oldSize = c.pos - pos;
            int newSize = sizeOf(v);
            adjust(newSize - oldSize);
            write(v);
            return old;
        }

        public void set(final LineRange v) {
            _set(v);
        }

        /**
         * Inserts the value at the current cursor position.
         */
        public void add(final LineRange v) {
            int newSize = sizeOf(v);
            adjust(newSize);
            write(v);
        }

        /**
         * Removes the current value at the cursor position.
         */
        public LineRange delete() {
            Cursor c = copy();
            LineRange old = c.next();
            adjust(pos - c.pos);
            return old;
        }

        private int sizeOf(final LineRange v) {
            return sizeOf(v.getStart()) + sizeOf(v.getEnd() - v.getStart());
        }

        /**
         * Computes the number of bytes that the value 'i' would occupy in its
         * encoded form.
         */
        private int sizeOf(int i) {
            int n = 0;
            do {
                i /= 0x80;
                n++;
            } while (i > 0);
            return n;
        }
    }

    /**
     * Makes sure that the buffer has capability to store N bytes.
     */
    private void ensure(final int n) {
        if (data.length < n) {
            byte[] buf = new byte[Math.max(n, data.length * 2)];
            System.arraycopy(data, 0, buf, 0, len);
            data = buf;
        }
    }

    @Override
    public boolean contains(final Object o) {
        if (o instanceof LineRange) {
            LineRange lr = (LineRange)o;

            for (Cursor c = new Cursor(); c.hasNext();) {
                if (c.compare(lr)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public LineRange get(final int index) {
        return new Cursor().skip(index).next();
    }

    @Override
    public int size() {
        return new Cursor().count();
    }

    @Override
    public LineRange set(final int index, final LineRange element) {
        return new Cursor().skip(index)._set(element);
    }

    @Override
    public void add(final int index, final LineRange element) {
        new Cursor().skip(index).add(element);
    }

    @Override
    public LineRange remove(final int index) {
        return new Cursor().skip(index).delete();
    }

    @Override
    public boolean add(final LineRange lr) {
        new Cursor(len).add(lr);
        return true;
    }

    @Override
    public void clear() {
        len = 0;
    }

    @Override
    public Iterator<LineRange> iterator() {
        return new Cursor();
    }

    @Override
    public ListIterator<LineRange> listIterator() {
        return new Cursor();
    }

    @Override
    public ListIterator<LineRange> listIterator(final int index) {
        return new Cursor().skip(index);
    }

    /**
     * Minimizes the memory waste by throwing away excess capacity.
     */
    public void trim() {
        if (len != data.length) {
            byte[] small = new byte[len];
            System.arraycopy(data, 0, small, 0, len);
            data = small;
        }
    }

    /**
     * {@link Converter} implementation for XStream.
     */
    @SuppressWarnings("rawtypes")
    public static final class ConverterImpl extends RobustCollectionConverter {
        public ConverterImpl(final XStream xs) {
            super(xs);
        }

        @Override
        public boolean canConvert(final Class type) {
            return type == LineRangeList.class;
        }

        @Override
        protected void populateCollection(final HierarchicalStreamReader reader,
                final UnmarshallingContext context, final Collection collection) {
            super.populateCollection(reader, context, collection);
            ((LineRangeList)collection).trim();
        }

        @Override
        protected Object createCollection(final Class type) {
            return new LineRangeList();
        }
    }

}