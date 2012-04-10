package hudson.plugins.analysis.util.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.model.Item;
import hudson.model.AbstractBuild;

import hudson.plugins.analysis.util.PackageDetectors;

/**
 *  A base class for annotations.
 *
 * @author Ulli Hafner
 */
@ExportedBean
@SuppressWarnings("PMD.CyclomaticComplexity")
public abstract class AbstractAnnotation implements FileAnnotation, Serializable { // NOPMD
    private static final String DEFAULT_PACKAGE = "Default Package";
    /** UNIX path separator. */
    private static final String SLASH = "/";
    /** Temporary directory holding the workspace files. */
    public static final String WORKSPACE_FILES = "workspace-files";
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -1092014926477547148L;
    /** Current key of this annotation. */
    private static long currentKey;

    /** The message of this annotation. */
    private final String message;
    /** The priority of this annotation. */
    private Priority priority;
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
     * Context hash code of this annotation. This hash code is used to decide if
     * two annotations are equal even if the equals method returns <code>false</code>.
     */
    private long contextHashCode;
    /** The origin of this warning. */
    private String origin;
    /** Relative path of this duplication. @since 1.10 */
    private String pathName;
    /** Column start of primary line range of warning. @since 1.38 */
    private int primaryColumnStart;
    /** Column end of primary line range of warning. @since 1.38 */
    private int primaryColumnEnd;

    /**
     * Creates a new instance of <code>AbstractAnnotation</code>.
     *
     * @param message
     *            the message of the warning
     * @param start
     *            the first line of the line range
     * @param end
     *            the last line of the line range
     * @param category
     *            the category of the annotation
     * @param type
     *            the type of the annotation
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("ST")
    public AbstractAnnotation(final String message, final int start, final int end, final String category, final String type) {
        this.message = StringUtils.strip(message);
        this.category = StringUtils.defaultString(category);
        this.type = StringUtils.defaultString(type);

        key = currentKey++;

        lineRanges = new ArrayList<LineRange>();
        lineRanges.add(new LineRange(start, end));
        primaryLineNumber = start;

        contextHashCode = currentKey;
    }

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
     * @param category
     *            the category of the annotation
     * @param type
     *            the type of the annotation
     */
    public AbstractAnnotation(final Priority priority, final String message, final int start, final int end,
            final String category, final String type) {
        this(message, start, end, category, type);
        this.priority = priority;
    }

    /**
     * Copy constructor: Creates a new instance of {@link AbstractAnnotation}.
     *
     * @param copy
     *            the annotation to copy the values from
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("ST")
    public AbstractAnnotation(final AbstractAnnotation copy) {
        key = currentKey++;

        message = copy.getMessage();
        priority = copy.getPriority();
        primaryLineNumber = copy.getPrimaryLineNumber();
        lineRanges = new ArrayList<LineRange>(copy.getLineRanges());

        contextHashCode = copy.getContextHashCode();

        fileName = copy.getFileName();
        category = copy.getCategory();
        type = copy.getType();
        moduleName = copy.getModuleName();
        packageName = copy.getPackageName();
    }

    /**
     * Sets the column position of this warning.
     *
     * @param columnStart
     *            starting column
     * @param columnEnd
     *            ending column
     */
    public void setColumnPosition(final int columnStart, final int columnEnd) {
        primaryColumnStart = columnStart;
        primaryColumnEnd = columnEnd;
    }

    /** {@inheritDoc} */
    public String getLinkName() {
        if (hasPackageName()) {
            return getPackageName() + "." + FilenameUtils.getBaseName(getFileName());
        }
        else {
            if (StringUtils.isBlank(pathName)) {
                return getFileName();
            }
            else {
                return pathName + SLASH + getShortFileName();
            }
        }
    }

    /** {@inheritDoc} */
    public boolean hasPackageName() {
        String actualPackageName = StringUtils.trim(packageName);

        return StringUtils.isNotBlank(actualPackageName) && !StringUtils.equals(actualPackageName, PackageDetectors.UNDEFINED_PACKAGE);
    }

    /**
     * Sets the pathname for this warning.
     *
     * @param workspacePath
     *            the workspace path
     */
    public void setPathName(final String workspacePath) {
        String normalized = workspacePath.replace('\\', '/');

        pathName = StringUtils.removeStart(getFileName(), normalized);
        pathName = StringUtils.remove(pathName, FilenameUtils.getName(getFileName()));
        pathName = StringUtils.removeStart(pathName, SLASH);
        pathName = StringUtils.removeEnd(pathName, SLASH);
    }

    /** {@inheritDoc} */
    public String getPathName() {
        return pathName;
    }

    /** {@inheritDoc} */
    public String getOrigin() {
        return StringUtils.defaultString(origin);
    }

    /**
     * Sets the origin of this annotation to the specified value.
     *
     * @param origin the value to set
     */
    public void setOrigin(final String origin) {
        this.origin = origin;
    }

    /**
     * Sets the priority to the specified value.
     *
     * @param priority the value to set
     */
    public void setPriority(final Priority priority) {
        this.priority = priority;
    }

    /** {@inheritDoc} */
    @Exported
    public String getMessage() {
        return message;
    }

    /** {@inheritDoc} */
    @Exported
    public Priority getPriority() {
        return priority;
    }

    /** {@inheritDoc} */
    @Exported
    public final long getKey() {
        return key;
    }

    /** {@inheritDoc} */
    @Exported
    public final String getFileName() {
        return fileName;
    }

    /** {@inheritDoc} */
    public String getTempName(final AbstractBuild<?, ?> owner) {
        if (fileName != null) {
            return owner.getRootDir().getAbsolutePath()
                    + SLASH + WORKSPACE_FILES
                    + SLASH + Integer.toHexString(fileName.hashCode()) + ".tmp";
        }
        return StringUtils.EMPTY;
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
        return StringUtils.defaultIfEmpty(packageName, DEFAULT_PACKAGE);
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
    @Exported
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

    /** {@inheritDoc} */
    public long getContextHashCode() {
        return contextHashCode;
    }

    /** {@inheritDoc} */
    public void setContextHashCode(final long contextHashCode) {
        this.contextHashCode = contextHashCode;
    }

    // CHECKSTYLE:OFF

    @Override
    @SuppressWarnings("PMD")
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
        result = prime * result + primaryColumnStart;
        result = prime * result + primaryColumnEnd;
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("PMD")
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
        if (primaryColumnStart != other.primaryColumnStart) {
            return false;
        }
        if (primaryColumnEnd != other.primaryColumnEnd) {
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
        return FilenameUtils.getName(fileName);
    }

    /**
     * Checks if the file exists and the user is authorized to see the contents of the file.
     *
     * @return <code>true</code>, if successful
     */
    public final boolean canDisplayFile(final AbstractBuild<?, ?> owner) {
        if (owner.hasPermission(Item.WORKSPACE)) {
            return new File(getFileName()).exists() || new File(getTempName(owner)).exists();
        }
        return false;
    }

    /** {@inheritDoc} */
    public int compareTo(final FileAnnotation other) {
        int result;

        result = getFileName().compareTo(other.getFileName());
        if (result != 0) {
            return result;
        }
        result = getPrimaryLineNumber() - other.getPrimaryLineNumber();
        if (result != 0) {
            return result;
        }

        return hashCode() - other.hashCode(); // fallback
    }

    @Override
    public String toString() {
        return String.format("%s(%s):%s,%s,%s:%s", getFileName(), primaryLineNumber, priority, getCategory(), getType(), getMessage());
    }
}
