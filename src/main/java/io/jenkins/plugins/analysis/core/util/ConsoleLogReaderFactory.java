package io.jenkins.plugins.analysis.core.util;

import java.io.IOException;
import java.io.Reader;

import com.google.errorprone.annotations.MustBeClosed;

import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;

import hudson.console.ConsoleNote;
import hudson.model.Run;

/**
 * Provides a reader factory for Jenkins' console log.
 *
 * @author Ullrich Hafner
 */
public class ConsoleLogReaderFactory extends ReaderFactory {
    private final Run<?, ?> run;

    /**
     * Creates a new {@link ConsoleLogReaderFactory}.
     *
     * @param run
     *         the run that provides the console log
     */
    public ConsoleLogReaderFactory(final Run<?, ?> run) {
        super(run.getCharset(), ConsoleNote::removeNotes);

        this.run = run;
    }

    /**
     * Returns the name of the resource.
     *
     * @return the file name
     */
    @Override
    public String getFileName() {
        return ConsoleLogHandler.JENKINS_CONSOLE_LOG_FILE_NAME_ID;
    }

    /**
     * Creates a new {@link Reader} for the file.
     *
     * @return a reader
     */
    @Override
    @MustBeClosed
    public Reader create() {
        try {
            return run.getLogReader();
        }
        catch (IOException e) {
            throw new ParsingException(e);
        }
    }
}
