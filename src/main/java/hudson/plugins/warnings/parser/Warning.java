package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.AbstractAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import org.apache.commons.lang.StringUtils;

/**
 * A serializable Java Bean class representing a warning.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * </p>
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class Warning extends AbstractAnnotation {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5171661552905752370L;

    /**
     * Creates a new instance of <code>Warning</code>.
     *
     * @param fileName
     *            the name of the file
     * @param start
     *            the first line of the line range
     * @param type
     *            the identifier of the warning type
     * @param category
     *            the warning category
     * @param message
     *            the message of the warning
     */
    public Warning(final String fileName, final int start, final String type, final String category, final String message) {
        this(fileName, start, type, category, message, Priority.NORMAL);
    }

    /**
     * Creates a new instance of <code>Warning</code>.
     *
     * @param fileName
     *            the name of the file
     * @param start
     *            the first line of the line range
     * @param type
     *            the identifier of the warning type
     * @param category
     *            the warning category
     * @param message
     *            the message of the warning
     * @param priority
     *            the priority of the warning
     */
    public Warning(final String fileName, final int start, final String type, final String category, final String message, final Priority priority) {
        super(priority, message, start, start, category, type);
        setFileName(fileName);
    }

    /**
     * Gets the associated file name of this bug (without path).
     *
     * @return the short file name
     */
    public String getShortFileName() {
        return StringUtils.substringAfterLast(getFileName(), "/");
    }

    /** {@inheritDoc} */
    public String getToolTip() {
        return StringUtils.EMPTY;
    }
}

