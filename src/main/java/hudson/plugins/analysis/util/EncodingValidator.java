package hudson.plugins.analysis.util;

import javax.annotation.CheckForNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.Charsets;
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
    private static Set<String> allCharacterSets;

    static {
        try {
            allCharacterSets = Collections.unmodifiableSet(new HashSet<>(
                    Charset.availableCharsets().keySet()));
        }
        // CHECKSTYLE:OFF
        catch (Exception exception) {
            allCharacterSets = Collections.emptySet();
        }
        // CHECKSTYLE:ON
    }

    /**
     * Returns all available character set names.
     *
     * @return all available character set names
     */
    public static Set<String> getAvailableCharsets() {
        return allCharacterSets;
    }

    /**
     * Returns the default charset for the specified encoding string. If the
     * default encoding is empty or <code>null</code>, or if the charset is not
     * valid then the default encoding of the platform is returned.
     *
     * @param defaultEncoding
     *            identifier of the character set
     * @return the default charset for the specified encoding string
     */
    public static Charset defaultCharset(@CheckForNull final String defaultEncoding) {
        try {
            if (StringUtils.isNotBlank(defaultEncoding)) {
                return Charset.forName(defaultEncoding);
            }
        }
        catch (UnsupportedCharsetException | IllegalCharsetNameException exception) {
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
    public static LineIterator readFile(final String fileName, @CheckForNull final String encoding)
            throws FileNotFoundException, IOException {
        FileInputStream stream = new FileInputStream(new File(fileName));
        if (StringUtils.isNotBlank(encoding)) {
            return IOUtils.lineIterator(stream, encoding);
        }
        else {
            return new LineIterator(new InputStreamReader(stream, Charsets.toCharset(Charset.defaultCharset())));
        }
    }

    /**
     * Validates a file encoding. The encoding must be an encoding ID supported
     * by the underlying Java platform.
     *
     * @param encoding
     *            the name of the encoding that will be checked
     * @return a positive {@link FormValidation} object
     * @throws FormValidation
     *             if the encoding is not valid
     */
    @Override
    public FormValidation check(final String encoding) throws FormValidation {
        try {
            if (StringUtils.isBlank(encoding) || Charset.forName(encoding).canEncode()) {
                return FormValidation.ok();
            }
        }
        catch (IllegalCharsetNameException | UnsupportedCharsetException exception) {
            // throw a FormValidation error
        }
        throw FormValidation.error(Messages.FieldValidator_Error_DefaultEncoding());
    }

    /**
     * Returns the encoding used to read a file. If the specified
     * encoding is empty or <code>null</code>, or if the encoding is not valid
     * then the default encoding of the platform is returned.
     *
     * @param encoding
     *            identifier of the character set
     * @return the default encoding for the specified encoding string
     */
    public static String getEncoding(@CheckForNull final String encoding) {
        if (StringUtils.isNotBlank(encoding) && Charset.forName(encoding).canEncode()) {
            return encoding;
        }
        return Charset.defaultCharset().name();
    }
}
