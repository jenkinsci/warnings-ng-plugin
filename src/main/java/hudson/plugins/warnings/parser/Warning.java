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
    /** Warning type. */
    private final String type;
    /** Warning category. */
    private final String category;

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
        super(priority, message, start, start);
        this.type = type;
        this.category = category;
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

    /**
     * Returns the category of the warning.
     *
     * @return the warning category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the warning type.
     *
     * @return the warning type
     */
    public String getType() {
        return type;
    }

    /** {@inheritDoc} */
    public String getToolTip() {
        return getMessage();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Warning other = (Warning)obj;
        if (category == null) {
            if (other.category != null) {
                return false;
            }
        }
        else if (!category.equals(other.category)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        }
        else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }
}

