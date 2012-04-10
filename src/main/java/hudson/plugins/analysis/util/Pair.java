package hudson.plugins.analysis.util;

/**
 * <code>Pair</code> is a helper class for the frequent case, when two objects
 * must be combined for a collection entry or even a key. The concept of a pair
 * is fundamental for all languages derived from LISP and is even useful in a
 * language like Java.
 * <p>
 * Note: <code>Pair</code>s may be used as keys for a hash collection, when
 * both elements, named head and tail, implements the hashing protocol.
 *
 * @param <H>
 *            Type of the head
 * @param <T>
 *            Type of the tail
 */
public final class Pair<H, T> {
    /** Head of the pair. */
    private H head;
    /** Tail of the pair. */
    private T tail;

    /**
     * Constructs a <code>Pair</code> using the two given objects as head and
     * tail.
     *
     * @param head
     *            the object to be used as the head of the pair
     * @param tail
     *            the object to be used as the tail of the pair
     */
    public Pair(final H head, final T tail) {
        this.head = head;
        this.tail = tail;
    }

    /**
     * Returns the head object of the pair.
     *
     * @return the head object of the pair
     * @see #setHead
     * @see #getTail
     */
    public H getHead() {
        return head;
    }

    /**
     * Modifies the head object of the pair.
     *
     * @param newHead
     *            the new head object of the pair
     * @see #getHead
     */
    public void setHead(final H newHead) {
        head = newHead;
    }

    /**
     * Returns the tail object of the pair.
     *
     * @return the tail object of the pair
     * @see #setTail
     * @see #getHead
     */
    public T getTail() {
        return tail;
    }

    /**
     * Modifies the tail object of the pair.
     *
     * @param newTail
     *            new tail object of the pair
     * @see #getTail
     */
    public void setTail(final T newTail) {
        tail = newTail;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        else if ((other != null) && (other.getClass() == getClass())) {
            return compareContents((Pair<?, ?>)other);
        }
        else {
            return false;
        }
    }

    /**
     * Compares the contents of this pair with the other pair.
     *
     * @param other
     *            the other pair
     * @return <code>true</code> if the contents are equal, <code>false</code>
     *         otherwise
     */
    private boolean compareContents(final Pair<?, ?> other) {
        if (head == null) {
            if (other.head != null) {
                return false;
            }
        }
        else if (!head.equals(other.head)) {
            return false;
        }

        if (tail == null) {
            if (other.tail != null) {
                return false;
            }
        }
        else if (!tail.equals(other.tail)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return ((head == null) ? 0 : head.hashCode()) + ((tail == null) ? 0 : tail.hashCode());
    }

    @Override
    public String toString() {
        return "<" + head + ":" + tail + ">";
    }
}
