package hudson.plugins.analysis.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Master-Slave transfer object for blame information.
 *
 * @author Ullrich Hafner
 */
public class BlameRequest implements Iterable<Integer>, Serializable {
    private String fileName;
    private final Set<Integer> lines = new HashSet<Integer>();
    private final Map<Integer, String> commitByLine = new HashMap<Integer, String>();
    private final Map<Integer, String> nameByLine = new HashMap<Integer, String>();
    private final Map<Integer, String> emailByLine = new HashMap<Integer, String>();

    public BlameRequest(final String fileName, final int lineNumber) {
        this.fileName = fileName;
        addLineNumber(lineNumber);
    }

    public final void addLineNumber(final int lineNumber) {
        lines.add(lineNumber);
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return fileName + " - " + lines;
    }

    @Override
    public Iterator<Integer> iterator() {
        return lines.iterator();
    }

    public void setCommit(final int line, final String id) {
        commitByLine.put(line, id);
    }

    public String getCommit(final int line) {
        return commitByLine.get(line);
    }

    public void setName(final int line, final String name) {
        nameByLine.put(line, name);
    }

    public String getName(final int line) {
        return nameByLine.get(line);
    }

    public void setEmail(final int line, final String emailAddress) {
        emailByLine.put(line, emailAddress);
    }

    public String getEmail(final int line) {
        return emailByLine.get(line);
    }
}
