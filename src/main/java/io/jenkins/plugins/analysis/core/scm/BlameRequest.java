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
    private String fileName;
    private final Set<Integer> lines = new HashSet<Integer>();
    private final Map<Integer, String> commitByLine = new HashMap<Integer, String>();
    private final Map<Integer, String> nameByLine = new HashMap<Integer, String>();
    private final Map<Integer, String> emailByLine = new HashMap<Integer, String>();

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

    private void add(final int lineNumber) {
        lines.add(lineNumber);
    }

    /**
     * Adds another line number to this request.
     *
     * @param lineNumber
     *         the line number to add
     */
    void addLineNumber(final int lineNumber) {
        add(lineNumber);
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
        commitByLine.put(lineNumber, id);
    }

    public String getCommit(final int line) {
        return commitByLine.get(line);
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
        nameByLine.put(lineNumber, name);
    }

    public String getName(final int line) {
        return nameByLine.get(line);
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
        emailByLine.put(lineNumber, emailAddress);
    }

    public String getEmail(final int line) {
        return emailByLine.get(line);
    }
}
