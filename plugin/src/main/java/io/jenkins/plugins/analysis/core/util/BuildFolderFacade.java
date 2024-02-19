package io.jenkins.plugins.analysis.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.stream.Stream;

import com.google.errorprone.annotations.MustBeClosed;

import edu.hm.hafner.analysis.Issue;

import hudson.model.Run;

/**
 * Facade to the files in the build folder of the Jenkins controller. Encapsulates all calls to the running Jenkins
 * server so that tests can replace this facade with a stub.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class BuildFolderFacade implements Serializable {
    private static final long serialVersionUID = 1904631270145841113L;

    /**
     * Returns the lines of the console log. If the log cannot be read, then the exception message is returned as text.
     *
     * @param build
     *         the build to get the console log for
     *
     * @return the lines of the console log
     */
    @MustBeClosed
    public Stream<String> readConsoleLog(final Run<?, ?> build) {
        return new ConsoleLogReaderFactory(build).readStream();
    }

    /**
     * Returns the affected file with the specified file name.
     *
     * @param build
     *         the build to read the files from
     * @param fileName
     *         the file to read
     * @param sourceEncoding
     *         the encoding to use when reading the file
     *
     * @return the affected file
     * @throws IOException
     *         if the file could not be read
     */
    @MustBeClosed
    public Reader readFile(final Run<?, ?> build, final String fileName, final Charset sourceEncoding)
            throws IOException {
        InputStream inputStream = AffectedFilesResolver.asStream(build, fileName);

        return new InputStreamReader(inputStream, sourceEncoding);
    }

    /**
     * Returns whether the affected file of the specified issue can be accessed.
     *
     * @param build
     *         the build to read the files from
     * @param issue
     *         the issue to check the affected file for
     *
     * @return {@code true} if the affected file of the specified issue can be accessed, {@code false} otherwise
     */
    public boolean canAccessAffectedFileOf(final Run<?, ?> build, final Issue issue) {
        return AffectedFilesResolver.hasAffectedFile(build, issue);
    }
}
