package hudson.plugins.warnings.util.model;

import java.io.Serializable;

/**
 * A line range in a source file is defined by its first and last line.
 *
 * @author Ulli Hafner
 */
public class LineRange implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -4124143085672930110L;

    /** Start of the range. */
    private final int start;
    /** End of the range. */
    private final int end;

    /**
     * Creates a new instance of <code>LineRange</code>. This range
     * represents a single line.
     *
     * @param line
     *            the first and last line of the range
     */
    public LineRange(final int line) {
        this(line, line);
    }

    /**
     * Creates a new instance of <code>LineRange</code>.
     *
     * @param start
     *            start of the range
     * @param end
     *            end of the range
     */
    public LineRange(final int start, final int end) {
        if (start <= 0) {
            this.start = 0;
            this.end = 0;
        }
        else if (start < end) {
            this.start = start;
            this.end = end;
        }
        else {
            this.start = end;
            this.end = start;
        }
    }

    /**
     * Returns the first line of this range.
     *
     * @return the first line of this range
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the last line of this range.
     *
     * @return the last line of this range
     */
    public int getEnd() {
        return end;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + start;
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LineRange other = (LineRange)obj;
        if (end != other.end) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        return true;
    }
}

