package hudson.plugins.analysis.util.model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.model.AbstractBuild;
import hudson.model.Item;
import hudson.model.Run;
import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.AbstractAnnotationParser;
import hudson.plugins.analysis.util.PackageDetectors;
import hudson.plugins.analysis.util.TreeString;
import hudson.plugins.analysis.util.TreeStringBuilder;

/**
 * A base class for annotations.
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
    public static final String DEFAULT_CATEGORY = "-";

    /** Current key of this annotation. */
    private static long currentKey = new Random().nextLong();

    /** The message of this annotation. */
    private /*almost final*/ TreeString message;
    /** The priority of this annotation. */
    private Priority priority;
    /** Unique key of this annotation. */
    private final long key;
    /**
     * The ordered list of line ranges that show the origin of the annotation in
     * the associated file. To save memory consumption, this can be
     * {@link ImmutableList}, in which case updates requires a new copy.
     */
    private final LineRangeList lineRanges;
    /** Primary line number of this warning, i.e., the start line of the first line range. */
    private final int primaryLineNumber;
    /** The filename of the class that contains this annotation. */
    private TreeString fileName;
    /** The name of the maven or ant module that contains this annotation. */
    private TreeString moduleName;
    /** The name of the package (or name space) that contains this annotation. */
    private TreeString packageName;
    /** Bug category. */
    private /*almost final*/ String category;
    /** Bug type. */
    private /*almost final*/ String type;
    /**
     * Context hash code of this annotation. This hash code is used to decide if
     * two annotations are equal even if the equals method returns <code>false</code>.
     */
    private long contextHashCode;
    /** The origin of this warning. */
    private String origin;
    /** Relative path of this duplication. @since 1.10 */
    private TreeString pathName;
    /** Column start of primary line range of warning. @since 1.38 */
    private int primaryColumnStart;
    /** Column end of primary line range of warning. @since 1.38 */
    private int primaryColumnEnd;
    /** The build number in which this annotation has been introduced. @since 1.72 */
    private int build;

    /** Who caused this annotation. */
    private String authorName;
    /** Who caused this annotation. */
    private String authorEmail;
    /** Which commit caused this annotation. */
    private String commitId;

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
    @SuppressFBWarnings("ST")
    public AbstractAnnotation(final String message, final int start, final int end, final String category, final String type) {
        this.message = TreeString.of(StringUtils.strip(message));
        this.category = defaultString(category);
        this.type = defaultString(type);

        key = currentKey++;

        lineRanges = new LineRangeList();
        lineRanges.add(new LineRange(start, end));
        primaryLineNumber = start;

        contextHashCode = currentKey;
    }

    private static String defaultString(final String value) {
        return StringUtils.defaultIfBlank(value, DEFAULT_CATEGORY);
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
    @SuppressFBWarnings("ST")
    public AbstractAnnotation(final FileAnnotation copy) {
        key = currentKey++;

        message = TreeString.of(copy.getMessage());
        priority = copy.getPriority();
        primaryLineNumber = copy.getPrimaryLineNumber();
        lineRanges = new LineRangeList(copy.getLineRanges());

        contextHashCode = copy.getContextHashCode();

        fileName = TreeString.of(copy.getFileName());
        category = copy.getCategory();
        type = copy.getType();
        moduleName = TreeString.of(copy.getModuleName());
        packageName = TreeString.of(copy.getPackageName());
        authorName = copy.getAuthorName();
        authorEmail = copy.getAuthorEmail();
        commitId = copy.getCommitId();
    }

    /**
     * Called after XStream de-serialization to improve the memory usage.
     * Ideally we'd like this to be protected, so that the subtype can call this
     * method, but some plugins that depends on this (such as findbugs) already
     * define "private Object readResolve()", so defining it as protected will
     * break those subtypes. So instead, we expose "superReadResolve" as the
     * protected entry point for this method.
     *
     * @return this
     */
    @SuppressFBWarnings("SE")
    private Object readResolve() {
        if (origin != null) {
            origin = origin.intern();
        }
        if (category != null) {
            category = category.intern();
        }
        if (type != null) {
            type = type.intern();
        }
        if (authorName != null) {
            authorName = authorName.intern();
        }
        if (authorEmail != null) {
            authorEmail = authorEmail.intern();
        }
        if (commitId != null) {
            commitId = commitId.intern();
        }
        return this;
    }

    /**
     * Simply calls {@link #readResolve()}.
     */
    protected void superReadResolve() {
        readResolve();
    }

    /**
     * {@link AbstractAnnotationParser} can call this method to let
     * {@link AbstractAnnotation}s to reduce their memory footprint by sharing
     * what they can share with other {@link AbstractAnnotation}s.
     *
     * @param builder
     *            caches previously used strings
     * @since 1.43
     */
    public void intern(final TreeStringBuilder builder) {
        lineRanges.trim();
        message = builder.intern(message);
        fileName = builder.intern(fileName);
        moduleName = builder.intern(moduleName);
        packageName = builder.intern(packageName);

        readResolve(); // String.intern some of the data fields
    }


    /**
     * Let {@link FileAnnotation}s share some of their internal data structure
     * to reduce memory footprint.
     *
     * @param annotations
     *            the annotations to compress
     * @return The same object as passed in the 'annotations' parameter to let
     *         this function used as a filter.
     */
    public static Collection<FileAnnotation> intern(final Collection<FileAnnotation> annotations) {
        TreeStringBuilder stringPool = new TreeStringBuilder();
        for (FileAnnotation annotation : annotations) {
            if (annotation instanceof AbstractAnnotation) {
                AbstractAnnotation aa = (AbstractAnnotation) annotation;
                aa.intern(stringPool);
            }
        }
        stringPool.dedup();
        return annotations;
    }

    /**
     * Sets the column position of this warning.
     *
     * @param column
     *            the column of this warning
     */
    @Whitelisted
    public void setColumnPosition(final int column) {
        setColumnPosition(column, column);
    }

    /**
     * Sets the column position of this warning.
     *
     * @param columnStart
     *            starting column
     * @param columnEnd
     *            ending column
     */
    @Whitelisted
    public void setColumnPosition(final int columnStart, final int columnEnd) {
        primaryColumnStart = columnStart;
        primaryColumnEnd = columnEnd;
    }

    @Override
    public String getLinkName() {
        if (hasPackageName()) {
            return getPackageName() + "." + FilenameUtils.getBaseName(getFileName());
        }
        else {
            if (pathName == null || pathName.isBlank()) {
                return getFileName();
            }
            else {
                return pathName + SLASH + getShortFileName();
            }
        }
    }

    @Override
    public boolean hasPackageName() {
        String actualPackageName = StringUtils.trim(TreeString.toString(packageName));

        return StringUtils.isNotBlank(actualPackageName) && !StringUtils.equals(actualPackageName, PackageDetectors.UNDEFINED_PACKAGE);
    }

    /**
     * Sets the pathname for this warning.
     *
     * @param workspacePath
     *            the workspace path
     */
    @Override
    public void setPathName(final String workspacePath) {
        String normalized = workspacePath.replace('\\', '/');

        String s = StringUtils.removeStart(getFileName(), normalized);
        s = StringUtils.remove(s, FilenameUtils.getName(getFileName()));
        s = StringUtils.removeStart(s, SLASH);
        s = StringUtils.removeEnd(s, SLASH);
        pathName = TreeString.of(s);
    }

    @Override
    public String getPathName() {
        return TreeString.toString(pathName);
    }

    @Override
    public String getOrigin() {
        return StringUtils.defaultString(origin);
    }

    @Override
    public int getColumnEnd() {
        return primaryColumnEnd;
    }

    @Override
    public int getColumnStart() {
        return primaryColumnStart;
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

    @Override
    @Exported
    public String getMessage() {
        return TreeString.toString(message);
    }

    @Override
    @Exported
    public Priority getPriority() {
        return priority;
    }

    @Override
    @Exported
    public final long getKey() {
        return key;
    }

    @Override
    @Exported
    public final String getFileName() {
        return TreeString.toString(fileName);
    }

    @Override
    public String getTempName(@Nonnull final Run<?, ?> owner) {
        if (fileName != null) {
            return owner.getRootDir().getAbsolutePath()
                    + SLASH + WORKSPACE_FILES
                    + SLASH + Integer.toHexString(fileName.hashCode()) + ".tmp";
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getType() {
        return type;
    }

    /**
     * Sets the file name to the specified value.
     *
     * @param fileName the value to set
     */
    @Override
    public final void setFileName(final String fileName) {
        this.fileName = TreeString.of(StringUtils.strip(fileName).replace('\\', '/'));
    }

    @Override
    @Exported
    public final String getModuleName() {
        return StringUtils.defaultIfEmpty(TreeString.toString(moduleName), "Default Module");
    }

    /**
     * Sets the module name to the specified value.
     *
     * @param moduleName the value to set
     */
    @Override
    public final void setModuleName(final String moduleName) {
        this.moduleName = TreeString.of(moduleName);
    }

    @Override
    @Exported
    public final String getPackageName() {
        if (hasPackageName()) {
            return TreeString.toString(packageName);
        }
        else {
            return StringUtils.defaultIfEmpty(TreeString.toString(pathName),
                    StringUtils.defaultIfEmpty(TreeString.toString(packageName), DEFAULT_PACKAGE));
        }
    }

    /**
     * Sets the package name to the specified value.
     *
     * @param packageName the value to set
     */
    public final void setPackageName(final String packageName) {
        this.packageName = TreeString.of(packageName);
    }

    @Override
    public final Collection<LineRange> getLineRanges() {
        return Collections.unmodifiableCollection(lineRanges);
    }

    /** {@inheritDoc} */
    public String getAuthorName() {
        return authorName;
    }

    /** {@inheritDoc} */
    public void setAuthorName(final String authorName) {
        this.authorName = defaultString(authorName);
    }

    /** {@inheritDoc} */
    public String getAuthorEmail() {
        return authorEmail;
    }

    /** {@inheritDoc} */
    public void setAuthorEmail(final String authorEmail) {
        this.authorEmail = defaultString(authorEmail);
    }

    /** {@inheritDoc} */
    public String getCommitId() {
        return commitId;
    }

    /** {@inheritDoc} */
    public void setCommitId(final String commitId) {
        this.commitId = defaultString(commitId);
    }

    @Override
    @Exported
    public final int getPrimaryLineNumber() {
        return primaryLineNumber;
    }

    /**
     * Returns the line number that should be shown on top of the source code view.
     *
     * @return the line number
     */
    public final int getLinkLineNumber() {
        return Math.max(1, primaryLineNumber - 10);
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

    @Override
    public long getContextHashCode() {
        return contextHashCode;
    }

    @Override
    public void setContextHashCode(final long contextHashCode) {
        this.contextHashCode = contextHashCode;
    }

    // CHECKSTYLE:OFF

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractAnnotation that = (AbstractAnnotation) o;

        if (primaryLineNumber != that.primaryLineNumber) {
            return false;
        }
        if (primaryColumnStart != that.primaryColumnStart) {
            return false;
        }
        if (primaryColumnEnd != that.primaryColumnEnd) {
            return false;
        }
        if (build != that.build) {
            return false;
        }
        if (message != null ? !message.equals(that.message) : that.message != null) {
            return false;
        }
        if (priority != that.priority) {
            return false;
        }
        if (lineRanges != null ? !lineRanges.equals(that.lineRanges) : that.lineRanges != null) {
            return false;
        }
        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) {
            return false;
        }
        if (moduleName != null ? !moduleName.equals(that.moduleName) : that.moduleName != null) {
            return false;
        }
        if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) {
            return false;
        }
        if (category != null ? !category.equals(that.category) : that.category != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (origin != null ? !origin.equals(that.origin) : that.origin != null) {
            return false;
        }
        if (pathName != null ? !pathName.equals(that.pathName) : that.pathName != null) {
            return false;
        }
        if (authorName != null ? !authorName.equals(that.authorName) : that.authorName != null) {
            return false;
        }
        if (authorEmail != null ? !authorEmail.equals(that.authorEmail) : that.authorEmail != null) {
            return false;
        }
        return commitId != null ? commitId.equals(that.commitId) : that.commitId == null;
    }

    @Override
    public int hashCode() {
        int result = message != null ? message.hashCode() : 0;
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (lineRanges != null ? lineRanges.hashCode() : 0);
        result = 31 * result + primaryLineNumber;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (moduleName != null ? moduleName.hashCode() : 0);
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (origin != null ? origin.hashCode() : 0);
        result = 31 * result + (pathName != null ? pathName.hashCode() : 0);
        result = 31 * result + primaryColumnStart;
        result = 31 * result + primaryColumnEnd;
        result = 31 * result + build;
        result = 31 * result + (authorName != null ? authorName.hashCode() : 0);
        result = 31 * result + (authorEmail != null ? authorEmail.hashCode() : 0);
        result = 31 * result + (commitId != null ? commitId.hashCode() : 0);
        return result;
    }

    // CHECKSTYLE:ON

    /**
     * Gets the associated file name of this bug (without path).
     *
     * @return the short file name
     */
    @Override
    public String getShortFileName() {
        if (isInConsoleLog()) {
            return Messages.ConsoleLog_Name();
        }
        return FilenameUtils.getName(TreeString.toString(fileName));
    }

    /**
     * Checks if the file exists and the user is authorized to see the contents of the file.
     *
     * @return <code>true</code>, if successful
     */
    @Override
    public final boolean canDisplayFile(final Run<?, ?> owner) {
        if (owner.hasPermission(Item.WORKSPACE)) {
            return isInConsoleLog() || new File(getFileName()).exists() || new File(getTempName(owner)).exists();
        }
        return false;
    }

    @Override
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
        result = getColumnStart() - other.getColumnStart();
        if (result != 0) {
            return result;
        }

        return hashCode() - other.hashCode(); // fallback
    }

    @Override
    public String toString() {
        return String.format("%s(%s):%s,%s,%s:%s", getFileName(), primaryLineNumber, priority, getCategory(), getType(), getMessage());
    }

    @Override
    public boolean isInConsoleLog() {
        return fileName == null || StringUtils.isBlank(fileName.toString());
    }

    @Override
    public void setBuild(final int build) {
        this.build = build;
    }

    @Override
    public int getBuild() {
        return build;
    }

    /**
     * @deprecated use {@link #getTempName(Run)} instead
     */
    @Deprecated
    public String getTempName(final AbstractBuild<?, ?> owner) {
        return getTempName((Run<?, ?>) owner);
    }
}
