package io.jenkins.plugins.analysis.core.charts;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Chart label showing the build date.
 *
 * @author Ullrich Hafner
 */
class LocalDateLabel implements Comparable<LocalDateLabel> {
    private final LocalDate date;
    private final DateTimeFormatter formatter;

    /**
     * Creates a new instance of {@link LocalDateLabel}.
     *
     * @param date
     *            the date of the build
     */
    LocalDateLabel(final LocalDate date) {
        this.date = date;
        formatter = DateTimeFormatter.ofPattern("MM-dd");
    }

    @Override
    public int compareTo(@NonNull final LocalDateLabel o) {
        return date.compareTo(o.date);
    }

    @Override
    public String toString() {
        return formatter.format(date);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalDateLabel that = (LocalDateLabel) o;
        return date.equals(that.date) && formatter.equals(that.formatter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, formatter);
    }
}

