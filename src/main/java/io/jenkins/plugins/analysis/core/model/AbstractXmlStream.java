package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;

import hudson.XmlFile;
import hudson.util.XStream2;

/**
 * Base class that provides the basic setup to read and write entities using {@link XStream}.
 *
 * @param <T>
 *         type of the entities
 *
 * @author Ullrich Hafner
 */
abstract class AbstractXmlStream<T> {
    private static final Logger LOGGER = Logger.getLogger(AbstractXmlStream.class.getName());
    private final Class<T> type;

    AbstractXmlStream(final Class<T> type) {
        this.type = type;
    }

    /**
     * Returns the default value that should be returned if the XML file is broken.
     *
     * @return the default value
     */
    abstract T createDefaultValue();

    /**
     * Creates a new {@link XStream2} to serialize an entity of the given type.
     *
     * @return the stream
     */
    abstract XStream2 createStream();

    T read(final Path file) {
        return readXml(createFile(file), createDefaultValue());
    }

    void write(final Path file, final T entity) {
        try {
            createFile(file).write(entity);
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to write entity to file " + file, exception);
        }
    }

    private XmlFile createFile(final Path file) {
        return new XmlFile(createStream(), file.toFile());
    }

    private T readXml(final XmlFile dataFile, final T defaultValue) {
        try {
            Object restored = dataFile.read();

            if (type.isInstance(restored)) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Loaded data file " + dataFile);
                }
                return type.cast(restored);
            }
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Failed to load " + dataFile + ", wrong type: " + restored);
            }
        }
        catch (IOException exception) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Failed to load " + dataFile, exception);
            }
        }
        return defaultValue; // fallback
    }
}
