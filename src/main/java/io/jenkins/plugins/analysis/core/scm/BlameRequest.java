package io.jenkins.plugins.analysis.core.scm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Master-Slave transfer object for blame information for a single file. This blame request defines all required line
 * numbers that should be queried for the specified file. The result of the request will be stored in this class as
 * well: commit ID, name and email of author (for each requested line).
 *
 * @author Ullrich Hafner
 */
public class BlameRequest implements Iterable<Integer>, Serializable {
    private static final long serialVersionUID = -7491390234189584964L;

    static final String EMPTY = "-";

    private final String fileName;
    private final Set<Integer> lines = new HashSet<>();
    private final Map<Integer, String> commitByLine = new HashMap<>();
    private final Map<Integer, String> nameByLine = new HashMap<>();
    private final Map<Integer, String> emailByLine = new HashMap<>();

    /**
     * Creates a new instance of {@link BlameRequest}.
     *
     * @param fileName
     *         the file name
     * @param lineNumber
     *         the line number
     */
    public BlameRequest(final String fileName, final int lineNumber) {
        this.fileName = fileName;

        add(lineNumber);
    }

    private BlameRequest add(final int lineNumber) {
        lines.add(lineNumber);
        return this;
    }

    /**
     * Adds another line number to this request.
     *
     * @param lineNumber
     *         the line number to add
     */
    BlameRequest addLineNumber(final int lineNumber) {
        return add(lineNumber);
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return fileName + " - " + lines;
    }

    @Override
    @NonNull
    public Iterator<Integer> iterator() {
        return lines.iterator();
    }

    /**
     * Sets the commit ID for the specified line number.
     *
     * @param lineNumber
     *         the line number
     * @param id
     *         the commit ID
     */
    void setCommit(final int lineNumber, final String id) {
        setInternedStringValue(commitByLine, lineNumber, id);
    }

    /**
     * Returns the commit ID for the specified line.
     *
     * @param line
     *         the affected line
     *
     * @return the commit ID
     */
    public String getCommit(final int line) {
        return getStringValue(commitByLine, line);
    }

    /**
     * Sets the author name for the specified line number.
     *
     * @param lineNumber
     *         the line number
     * @param name
     *         the author name
     */
    void setName(final int lineNumber, final String name) {
        setInternedStringValue(nameByLine, lineNumber, name);
    }

    /**
     * Returns the author name for the specified line.
     *
     * @param line
     *         the affected line
     *
     * @return the author name
     */
    public String getName(final int line) {
        return getStringValue(nameByLine, line);
    }

    /**
     * Sets the email address for the specified line number.
     *
     * @param lineNumber
     *         the line number
     * @param emailAddress
     *         the email address of the author
     */
    void setEmail(final int lineNumber, final String emailAddress) {
        setInternedStringValue(emailByLine, lineNumber, emailAddress);
    }

    /**
     * Returns the author email for the specified line.
     *
     * @param line
     *         the affected line
     *
     * @return the author email
     */
    public String getEmail(final int line) {
        return getStringValue(emailByLine, line);
    }

    private String getStringValue(final Map<Integer, String> map, final int line) {
        if (map.containsKey(line)) {
            return map.get(line);
        }
        return EMPTY;
    }

    private void setInternedStringValue(final Map<Integer, String> map, final int lineNumber, final String value) {
        map.put(lineNumber, value.intern());
    }

    /**
     * Merges the lines of the other blame request with the lines of this instance.
     *
     * @param otherRequest
     *         the other request
     *
     * @throws IllegalArgumentException
     *         if the file name of the other request does not match
     */
    public void merge(final BlameRequest otherRequest) {
        if (otherRequest.getFileName().equals(getFileName())) {
            for (Integer otherLine : otherRequest) {
                if (!lines.contains(otherLine)) {
                    lines.add(otherLine);
                    setInternedStringValue(commitByLine, otherLine, otherRequest.getCommit(otherLine));
                    setInternedStringValue(nameByLine, otherLine, otherRequest.getName(otherLine));
                    setInternedStringValue(emailByLine, otherLine, otherRequest.getEmail(otherLine));
                }
            }
        }
        else {
            throw new IllegalArgumentException(
                    String.format("File names of this instance: %s, other file name %s",
                            getFileName(), otherRequest.getFileName()));
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BlameRequest request = (BlameRequest) o;

        if (!fileName.equals(request.fileName)) {
            return false;
        }
        if (!lines.equals(request.lines)) {
            return false;
        }
        if (!commitByLine.equals(request.commitByLine)) {
            return false;
        }
        if (!nameByLine.equals(request.nameByLine)) {
            return false;
        }
        return emailByLine.equals(request.emailByLine);
    }

    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 31 * result + lines.hashCode();
        result = 31 * result + commitByLine.hashCode();
        result = 31 * result + nameByLine.hashCode();
        result = 31 * result + emailByLine.hashCode();
        return result;
    }
}
