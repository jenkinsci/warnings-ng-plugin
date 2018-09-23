package io.jenkins.plugins.analysis.core.scm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

import com.google.errorprone.annotations.FormatMethod;

/**
 * Provides access to the blame information of a file. 
 *
 * @author Ullrich Hafner
 */
public class Blames implements Serializable {
    private static final long serialVersionUID = -7884822502506035784L;

    private final Map<String, BlameRequest> blamesPerFile = new HashMap<>();
    private final List<String> infoMessages = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();
    
    public boolean contains(final String fileName) {
        return blamesPerFile.containsKey(fileName);
    }

    public void addLine(final String fileName, final int lineStart) {
        BlameRequest request = blamesPerFile.get(fileName);
        request.addLineNumber(lineStart);
    }

    public void addRequest(final String fileName, final BlameRequest blameRequest) {
        blamesPerFile.put(fileName, blameRequest);
    }

    public boolean isEmpty() {
        return blamesPerFile.isEmpty();
    }

    public int size() {
        return blamesPerFile.size();
    }

    public Set<String> getFiles() {
        return blamesPerFile.keySet();
    }

    public BlameRequest getRequest(final String fileName) {
        return blamesPerFile.get(fileName);
    }

    public Collection<BlameRequest> getRequests() {
        return blamesPerFile.values();
    }

    /**
     * Logs the specified information message. Use this method to log any useful information when composing this instance.
     *
     * @param format
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     * @param args
     *         Arguments referenced by the format specifiers in the format string.  If there are more arguments than
     *         format specifiers, the extra arguments are ignored.  The number of arguments is variable and may be
     *         zero.
     *
     * @see #getInfoMessages()
     */
    @FormatMethod
    public void logInfo(final String format, final Object... args) {
        infoMessages.add(String.format(format, args));
    }

    /**
     * Logs the specified error message. Use this method to log any error when composing this instance.
     *
     * @param format
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     * @param args
     *         Arguments referenced by the format specifiers in the format string.  If there are more arguments than
     *         format specifiers, the extra arguments are ignored.  The number of arguments is variable and may be
     *         zero.
     *
     * @see #getInfoMessages()
     */
    @FormatMethod
    public void logError(final String format, final Object... args) {
        errorMessages.add(String.format(format, args));
    }

    /**
     * Returns the info messages that have been reported since the creation of this set of issues.
     *
     * @return the info messages
     */
    public ImmutableList<String> getInfoMessages() {
        return Lists.immutable.ofAll(infoMessages);
    }

    /**
     * Returns the error messages that have been reported since the creation of this set of issues.
     *
     * @return the error messages
     */
    public ImmutableList<String> getErrorMessages() {
        return Lists.immutable.ofAll(errorMessages);
    }

    public void add(final Blames blames) {
        // FIXME: we need to actually merge the results
        blamesPerFile.putAll(blames.blamesPerFile); 
    }

    public BlameRequest getFile(final String fileName) {
        return blamesPerFile.get(fileName);
    }
}
