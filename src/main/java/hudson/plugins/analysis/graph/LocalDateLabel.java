package hudson.plugins.analysis.graph;

import org.joda.time.LocalDate;

/**
 * Graph label showing the build date.
 *
 * @author Ulli Hafner
 */
public class LocalDateLabel implements Comparable<LocalDateLabel> {
    private final LocalDate date;

    /**
     * Creates a new instance of {@link LocalDateLabel}.
     *
     * @param date
     *            the date of the build
     */
    public LocalDateLabel(final LocalDate date) {
        this.date = date;
    }

    /** {@inheritDoc} */
    public int compareTo(final LocalDateLabel o) {
        return date.compareTo(o.date);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return date.toString("MM-dd");
    }
}

