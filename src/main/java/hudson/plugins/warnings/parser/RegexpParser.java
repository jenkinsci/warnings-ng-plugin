package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.JavaPackageDetector;
import hudson.plugins.warnings.util.model.FileAnnotation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/**
 * Parses an input stream for compiler warnings using the provided regular expression.
 *
 * @author Ulli Hafner
 */
public abstract class RegexpParser implements WarningsParser {
    /** Warning classification. */
    protected static final String DEPRECATION = "Deprecation";
    /** Warning classification. */
    protected static final String PROPRIETARY_API = "Proprietary API";
    /** Pattern of compiler warnings. */
    private final Pattern pattern;

    /**
     * Creates a new instance of <code>RegexpParser</code>.
     * @param warningPattern pattern of compiler warnings.
     */
    public RegexpParser(final String warningPattern) {
        pattern = Pattern.compile(warningPattern);
    }

    /**
     * Parses the specified input stream for compiler warnings using the provided regular expression.
     *
     * @param file the file to parse
     * @return the collection of annotations
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Collection<FileAnnotation> parse(final InputStream file) throws IOException {
        LineIterator iterator = IOUtils.lineIterator(file, null);

        ArrayList<FileAnnotation> warnings = new ArrayList<FileAnnotation>();
        while (iterator.hasNext()) {
            String line = iterator.nextLine();
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                Warning warning = createWarning(matcher);
                if (!warning.hasPackageName()) {
                    String packageName = new JavaPackageDetector().detectPackageName(warning.getFileName());
                    warning.setPackageName(packageName);
                }
                warnings.add(warning);
            }
        }

        return warnings;
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    protected abstract Warning createWarning(final Matcher matcher);

    /**
     * Converts a string line number to an integer value. If the string is not a valid line number,
     * then 0 is returned which indicates a warning at the top of the file.
     *
     * @param lineNumber the line number (as a string)
     * @return the line number
     */
    protected final int getLineNumber(final String lineNumber) {
        try {
            return Integer.parseInt(lineNumber);
        }
        catch (NumberFormatException exception) {
            return 0;
        }
    }

    /**
     * Classifies the warning message: tries to guess a category from the warning message.
     *
     * @param message the message to check
     * @return warning category, empty string if unknown
     */
    protected String classifyWarning(final String message) {
        if (StringUtils.contains(message, "proprietary")) {
            return PROPRIETARY_API;
        }
        if (StringUtils.contains(message, "deprecated")) {
            return DEPRECATION;
        }
        return StringUtils.EMPTY;
    }
}
