package hudson.plugins.analysis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import hudson.plugins.analysis.Messages;

import hudson.util.FormValidation;

/**
 * Validates a file encoding. The encoding must be an encoding ID supported by
 * the underlying Java platform.
 *
 * @author Ulli Hafner
 */
public class EncodingValidator implements Validator {
    /** All available character sets. */
    private static final Set<String> ALL_CHARSETS = Collections.unmodifiableSet(new HashSet<String>(Charset.availableCharsets().keySet()));

    /**
     * Returns all available character set names.
     *
     * @return all available character set names
     */
    public static Set<String> getAvailableCharsets() {
        return ALL_CHARSETS;
    }

    /**
     * Returns the default charset for the specified encoding string. If the
     * default encoding is empty or <code>null</code>, or if the charset is not
     * valid then the default encoding of the platform is returned.
     *
     * @param defaultEncoding identifier of the character set
     *
     * @return the default charset for the specified encoding string
     */
    public static Charset defaultCharset(final String defaultEncoding) {
        try {
            if (StringUtils.isNotBlank(defaultEncoding)) {
                return Charset.forName(defaultEncoding);
            }
        }
        catch (UnsupportedCharsetException exception) {
            // ignore and return default
        }
        catch (IllegalCharsetNameException exception) {
            // ignore and return default
        }
        return Charset.defaultCharset();
    }

    /**
     * Reads the specified file with the given encoding.
     *
     * @param fileName
     *            the file name
     * @param encoding
     *            the encoding of the file, if <code>null</code> or empty then
     *            the default encoding of the platform is used
     * @return the line iterator
     * @throws FileNotFoundException
     *             Indicates that the file is not found.
     * @throws IOException
     *             Signals that an I/O exception has occurred during reading of
     *             the file.
     */
    public static LineIterator readFile(final String fileName, final String encoding) throws FileNotFoundException, IOException {
        FileInputStream stream = new FileInputStream(new File(fileName));
        if (StringUtils.isNotBlank(encoding)) {
            return IOUtils.lineIterator(stream, encoding);
        }
        else {
            return IOUtils.lineIterator(stream, null);
        }
    }

    /**
     * Validates a file encoding. The encoding must be an encoding ID supported
     * by the underlying Java platform.
     */
    public FormValidation check(final String encoding) throws FormValidation {
        try {
            if (StringUtils.isBlank(encoding) || Charset.forName(encoding) != null) {
                return FormValidation.ok();
            }
        }
        catch (IllegalCharsetNameException exception) {
            // throw a FormValidation error
        }
        catch (UnsupportedCharsetException exception) {
            // throw a FormValidation error
        }
        throw FormValidation.error(Messages.FieldValidator_Error_DefaultEncoding());
    }
}

