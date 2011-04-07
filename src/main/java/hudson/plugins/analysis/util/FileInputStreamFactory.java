package hudson.plugins.analysis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.tools.ant.types.FileSet;

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

    /**
     * Returns all file names that match the specified pattern.
     *
     * @param root
     *            root directory to start the search from
     * @param pattern
     *            the Ant {@link FileSet} pattern to search for
     * @return the found file names
     */
    String[] find(File root, String pattern);
}

