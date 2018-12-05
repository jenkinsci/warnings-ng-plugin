package io.jenkins.plugins.analysis.core.model;

import java.io.Reader;
import java.nio.charset.Charset;

import edu.hm.hafner.analysis.ReaderFactory;
import io.jenkins.plugins.analysis.core.views.ConsoleDetail;

/**
 * Provides a reader factory for Jenkins' console log.
 *
 * @author Ullrich Hafner
 */
public class ConsoleLogReaderFactory extends ReaderFactory {
    private final Reader reader;

    /**
     * Creates a new {@link ConsoleLogReaderFactory}.
     *
     * @param reader
     *         the reader to the console log
     * @param charset
     *         the character encoding of the console log
     */
    public ConsoleLogReaderFactory(final Reader reader, final Charset charset) {
        super(charset);

        this.reader = reader;
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
        return reader;
    }
}
