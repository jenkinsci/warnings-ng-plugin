package io.jenkins.plugins.analysis.core.scm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

import com.google.errorprone.annotations.FormatMethod;

/**
 * Provides access to the blame information of report. Collects all blames for a set of affected files. Additionally,
 * info and error messages during the SCM processing will be stored.
 *
 * @author Ullrich Hafner
 */
public class Blames implements Serializable {
    private static final long serialVersionUID = -7884822502506035784L;

    private final Map<String, BlameRequest> blamesPerFile = new HashMap<>();
    private final List<String> infoMessages = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();
    private final String workspace;

    /**
     * Creates an empty instance of {@link Blames}.
     */
    public Blames() {
        this(StringUtils.EMPTY);
    }

    /**
     * Creates an empty instance of {@link Blames} that will work on the specified workspace. 
     */
    public Blames(final String workspace) {
        this.workspace = workspace;
    }

    
    /**
     * Returns whether there are files with blames in this instance.
     *
     * @return {@code true} if there a no blames available, {@code false} otherwise
     */
    public boolean isEmpty() {
        return blamesPerFile.isEmpty();
    }

    /**
     * Returns the number of files that have been added to this instance.
     *
     * @return number of affected files with blames
     */
    public int size() {
        return blamesPerFile.size();
    }

    /**
     * Returns whether the specified file already has been added.
     *
     * @param fileName
     *         the name of the file
     *
     * @return {@code true} if the file already has been added, {@code false} otherwise
     */
    public boolean contains(final String fileName) {
        return blamesPerFile.containsKey(fileName);
    }

    /**
     * Adds a blame request for the specified affected file and line number.
     *
     * @param fileName
     *         the absolute file name that will be used as a key
     * @param lineStart
     *         the line number to find the blame for
     */
    public void addLine(final String fileName, final int lineStart) {
        if (!contains(fileName)) {
            if (fileName.startsWith(workspace)) {
                String relativeFileName = fileName.substring(workspace.length());
                String cleanFileName = StringUtils.removeStart(relativeFileName, "/");
                blamesPerFile.put(fileName, new BlameRequest(cleanFileName, lineStart));
            }
            else {
                int error = errorMessages.size();
                if (error < 5) {
                    logError("Skipping non-workspace file %s (workspace = %s).%n", fileName, workspace);
                }
                else if (error == 5) {
                    logError("  ... skipped logging of additional non-workspace file errors ...");
                }
            }
        }
        else {
            BlameRequest request = blamesPerFile.get(fileName);
            request.addLineNumber(lineStart);
        }
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
     * Logs the specified information message. Use this method to log any useful information when composing this
     * instance.
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

    /**
     * Clears all info and error messages.
     */
    public void clearMessages() {
        errorMessages.clear();
        infoMessages.clear();
    }
}
