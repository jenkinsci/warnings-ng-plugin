package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.io.Reader;

import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;
import io.jenkins.plugins.analysis.core.views.ConsoleDetail;

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
        return ConsoleDetail.JENKINS_CONSOLE_LOG;
    }

    /**
     * Creates a new {@link Reader} for the file.
     *
     * @return a reader
     */
    @Override
    public Reader create() {
        try {
            return run.getLogReader();
        }
        catch (IOException e) {
            throw new ParsingException(e);
        }
    }
}
