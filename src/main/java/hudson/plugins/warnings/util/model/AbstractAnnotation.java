package hudson.plugins.warnings.util.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 *  A base class for annotations.
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public abstract class AbstractAnnotation implements FileAnnotation, Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -1092014926477547148L;
    /** Current key of this annotation. */
    private static long currentKey;

    /** The message of this annotation. */
    private final String message;
    /** The priority of this annotation. */
    private final Priority priority;
    /** Unique key of this annotation. */
    private final long key;
    /** The ordered list of line ranges that show the origin of the annotation in the associated file. */
    private final List<LineRange> lineRanges;
    /** Primary line number of this warning, i.e., the start line of the first line range. */
    private final int primaryLineNumber;
    /** The filename of the class that contains this annotation. */
    private String fileName;
    /** The name of the maven or ant module that contains this annotation. */
    private String moduleName;
    /** The name of the package (or name space) that contains this annotation. */
    private String packageName;
    /** Bug category. */
    private final String category;
    /** Bug type. */
    private final String type;

    /**
     * Creates a new instance of <code>AbstractAnnotation</code>.
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param start
     *            the first line of the line range
     * @param end
     *            the last line of the line range
     */
    public AbstractAnnotation(final Priority priority, final String message, final int start, final int end,
            final String category, final String type) {
        this.priority = priority;
        this.message = StringUtils.strip(message);
        this.category = StringUtils.defaultString(category);
        this.type = StringUtils.defaultString(type);

        key = currentKey++;

        lineRanges = new ArrayList<LineRange>();
        lineRanges.add(new LineRange(start, end));
        primaryLineNumber = start;
    }

    /** {@inheritDoc} */
    public String getMessage() {
        return message;
    }

    /** {@inheritDoc} */
    public Priority getPriority() {
        return priority;
    }

    /** {@inheritDoc} */
    public final long getKey() {
        return key;
    }

    /** {@inheritDoc} */
    public final String getFileName() {
        return fileName;
    }

    /** {@inheritDoc} */
    public String getCategory() {
        return category;
    }

    /** {@inheritDoc} */
    public String getType() {
        return type;
    }

    /**
     * Sets the file name to the specified value.
     *
     * @param fileName the value to set
     */
    public final void setFileName(final String fileName) {
        this.fileName = StringUtils.strip(fileName).replace('\\', '/');
    }

    /** {@inheritDoc} */
    public final String getModuleName() {
        return StringUtils.defaultIfEmpty(moduleName, "Default Module");
    }

    /**
     * Sets the module name to the specified value.
     *
     * @param moduleName the value to set
     */
    public final void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    /** {@inheritDoc} */
    public final String getPackageName() {
        return StringUtils.defaultIfEmpty(packageName, "Default Package");
    }

    /**
     * Sets the package name to the specified value.
     *
     * @param packageName the value to set
     */
    public final void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    /** {@inheritDoc} */
    public final Collection<LineRange> getLineRanges() {
        return Collections.unmodifiableCollection(lineRanges);
    }

    /** {@inheritDoc} */
    public final int getPrimaryLineNumber() {
        return primaryLineNumber;
    }

    /**
     * Adds another line range to this annotation.
     *
     * @param lineRange
     *            the line range to add
     */
    public void addLineRange(final LineRange lineRange) {
        if (!lineRanges.contains(lineRange)) {
            lineRanges.add(lineRange);
        }
    }

    // CHECKSTYLE:OFF

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + ((lineRanges == null) ? 0 : lineRanges.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
        result = prime * result + primaryLineNumber;
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractAnnotation other = (AbstractAnnotation)obj;
        if (category == null) {
            if (other.category != null) {
                return false;
            }
        }
        else if (!category.equals(other.category)) {
            return false;
        }
        if (fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        }
        else if (!fileName.equals(other.fileName)) {
            return false;
        }
        if (lineRanges == null) {
            if (other.lineRanges != null) {
                return false;
            }
        }
        else if (!lineRanges.equals(other.lineRanges)) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        }
        else if (!message.equals(other.message)) {
            return false;
        }
        if (moduleName == null) {
            if (other.moduleName != null) {
                return false;
            }
        }
        else if (!moduleName.equals(other.moduleName)) {
            return false;
        }
        if (packageName == null) {
            if (other.packageName != null) {
                return false;
            }
        }
        else if (!packageName.equals(other.packageName)) {
            return false;
        }
        if (primaryLineNumber != other.primaryLineNumber) {
            return false;
        }
        if (priority == null) {
            if (other.priority != null) {
                return false;
            }
        }
        else if (!priority.equals(other.priority)) {
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

    /**
     * Gets the associated file name of this bug (without path).
     *
     * @return the short file name
     */
    public String getShortFileName() {
        return StringUtils.substringAfterLast(getFileName(), "/");
    }
}
