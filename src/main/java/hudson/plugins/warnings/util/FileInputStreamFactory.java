package hudson.plugins.warnings.util;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Factory to create an {@link InputStream} from an absolute filename.
 *
 * @author Ulli Hafner
 */
public interface FileInputStreamFactory {
    /**
     * Creates an {@link InputStream} from the specified filename.
     *
     * @param fileName
     *            the file name
     * @return the input stream
     * @throws FileNotFoundException
     *             if the file could not be found
     */
    InputStream create(String fileName) throws FileNotFoundException;
}

