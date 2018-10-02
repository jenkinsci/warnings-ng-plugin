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

import edu.hm.hafner.util.NoSuchElementException;
import edu.umd.cs.findbugs.annotations.CheckForNull;

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
        this.workspace = normalizeFileName(workspace);
    }

    private String normalizeFileName(@CheckForNull final String platformFileName) {
        return StringUtils.replace(StringUtils.strip(platformFileName), "\\", "/");
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
     * Adds a blame request for the specified affected file and line number. This file and line will be processed by Git
     * blame later on.
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

    /**
     * Returns the absolute file names of the affected files that will be processed by Git blame.
     *
     * @return the file names
     */
    public Set<String> getFiles() {
        return blamesPerFile.keySet();
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

    /**
     * Returns all stored requests.
     *
     * @return the requests
     */
    public Collection<BlameRequest> getRequests() {
        return blamesPerFile.values();
    }

    /**
     * Returns the blames for the specified file.
     *
     * @param fileName
     *         absolute file name
     *
     * @return the blames for that file
     * @throws NoSuchElementException if the file name is not registered
     */
    public BlameRequest get(final String fileName) {
        if (contains(fileName)) {
            return blamesPerFile.get(fileName);
        }
        throw new NoSuchElementException("No information for file %s stored", fileName);
    }

    /**
     * Merges all specified blames with the current set of blames.
     * 
     * @param other the blames to add
     */
    public void addAll(final Blames other) {
        for (String otherFile : other.blamesPerFile.keySet()) {
            BlameRequest otherRequest = other.get(otherFile);
            if (contains(otherFile)) {
                get(otherFile).merge(otherRequest);
            }
            else {
                blamesPerFile.put(otherFile, otherRequest);
            }
        }
    }

    /**
     * Clears all info and error messages.
     */
    public void clearMessages() {
        errorMessages.clear();
        infoMessages.clear();
    }
}
