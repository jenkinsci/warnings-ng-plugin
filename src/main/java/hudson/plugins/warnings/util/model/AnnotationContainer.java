package hudson.plugins.warnings.util.model;

import hudson.plugins.warnings.util.Messages;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * A container for annotations.
 *
 * @author Ulli Hafner
 */
public class AnnotationContainer implements AnnotationProvider, Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 855696821788264261L;

    /** The annotations mapped by their key. */
    @SuppressWarnings("Se")
    private final Map<Long, FileAnnotation> annotations = new HashMap<Long, FileAnnotation>();
    /** The annotations mapped by priority. */
    private transient Map<Priority, Set<FileAnnotation>> annotationsByPriority;
    /** Determines whether to build up a set of {@link WorkspaceFile}s. */
    private boolean handleFiles;
    /** The files that contain annotations mapped by file name. */
    private transient Map<String, WorkspaceFile> filesByName;
    /** Name of this container. */
    private String name;

    /**
     * Creates a new instance of <code>AnnotationContainer</code>.
     */
    public AnnotationContainer() {
        this(false, StringUtils.EMPTY);
    }

    /**
     * Returns the name of this container.
     *
     * @return the name of this container
     */
    public final String getName() {
        return name;
    }

    /**
     * Sets the name of this container.
     *
     * @param name the name of this container
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * Creates a new instance of <code>AnnotationContainer</code>.
     *
     * @param handleFiles
     *            determines whether to build up a set of {@link WorkspaceFile}s.
     *            If set to <code>true</code> then this container will
     *            automatically build up a workspace file mapping that could be
     *            used by clients of this class. Set this value to
     *            <code>false</code> if your subclass already has such a
     *            mapping or provides a faster implementation of the associated
     *            methods {@link #getFiles()} and {@link #getFile(String)}.
     * @param name the name of this container
     */
    protected AnnotationContainer(final boolean handleFiles, final String name) {
        initialize(handleFiles);
        this.name = name;
    }

    /**
     * Initializes the priorities maps and the filename to file mapping.
     *
     * @param handleFilesByContainer
     *            determines whether to build up a set of {@link WorkspaceFile}s.
     *            If set to <code>true</code> then this container will
     *            automatically build up a workspace file mapping that could be
     *            used by clients of this class. Set this value to
     *            <code>false</code> if your subclass already has such a
     *            mapping or provides a faster implementation of the associated
     *            methods {@link #getFiles()} and {@link #getFile(String)}.
     */
    private void initialize(final boolean handleFilesByContainer) {
        annotationsByPriority = new EnumMap<Priority, Set<FileAnnotation>>(Priority.class);
        for (Priority priority : Priority.values()) {
            annotationsByPriority.put(priority, new HashSet<FileAnnotation>());
        }
        filesByName = new HashMap<String, WorkspaceFile>();
        handleFiles = handleFilesByContainer;
    }

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve() {
        rebuildMappings(handleFiles);
        return this;
    }

    /**
     * Rebuilds the priorities and files after deserialization.
     *
     * @param handleFilesByContainer
     *            determines whether to build up a set of {@link WorkspaceFile}s.
     *            If set to <code>true</code> then this container will
     *            automatically build up a workspace file mapping that could be
     *            used by clients of this class. Set this value to
     *            <code>false</code> if your subclass already has such a
     *            mapping or provides a faster implementation of the associated
     *            methods {@link #getFiles()} and {@link #getFile(String)}.
     */
    protected void rebuildMappings(final boolean handleFilesByContainer) {
        initialize(handleFilesByContainer);
        for (FileAnnotation annotation : getAnnotations()) {
            annotationsByPriority.get(annotation.getPriority()).add(annotation);
            if (handleFilesByContainer) {
                addFile(annotation);
            }
        }
    }

    /**
     * Adds a new file to this container that will contain the specified
     * annotation. If the file already exists, then the annotation is only added
     * to this file.
     *
     * @param annotation the new annotation
     */
    protected final void addFile(final FileAnnotation annotation) {
        String fileName = annotation.getFileName();
        if (!filesByName.containsKey(fileName)) {
            filesByName.put(fileName, new WorkspaceFile(fileName));
        }
        filesByName.get(fileName).addAnnotation(annotation);
    }

    /**
     * Adds the specified annotation to this container.
     *
     * @param annotation
     *            the annotation to add
     */
    public final void addAnnotation(final FileAnnotation annotation) {
        annotations.put(annotation.getKey(), annotation);
        annotationsByPriority.get(annotation.getPriority()).add(annotation);
        annotationAdded(annotation);
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations
     *            the annotations to add
     */
    public final void addAnnotations(final Collection<? extends FileAnnotation> newAnnotations) {
        for (FileAnnotation annotation : newAnnotations) {
            addAnnotation(annotation);
        }
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations
     *            the annotations to add
     */
    public final void addAnnotations(final FileAnnotation[] newAnnotations) {
        addAnnotations(Arrays.asList(newAnnotations));
    }

    /**
     * Called if the specified annotation has been added to this container.
     * Subclasses may override this default empty implementation.
     *
     * @param annotation
     *            the added annotation
     */
    protected void annotationAdded(final FileAnnotation annotation) {
        if (handleFiles) {
            addFile(annotation);
        }
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations() {
        return Collections.unmodifiableCollection(annotations.values());
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations(final Priority priority) {
        return Collections.unmodifiableCollection(annotationsByPriority.get(priority));
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations(final String priority) {
        return getAnnotations(getPriority(priority));
    }

    /**
     * Converts a String priority to an actual enumeration value.
     *
     * @param priority
     *            priority as a String
     * @return enumeration value.
     */
    private Priority getPriority(final String priority) {
        return Priority.fromString(priority);
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations() {
        return annotations.size();
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations(final Priority priority) {
        return annotationsByPriority.get(priority).size();
    }

    /** {@inheritDoc} */
    public final int getNumberOfAnnotations(final String priority) {
        return getNumberOfAnnotations(getPriority(priority));
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations() {
        return !annotations.isEmpty();
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations(final Priority priority) {
        return !annotationsByPriority.get(priority).isEmpty();
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations(final String priority) {
        return hasAnnotations(getPriority(priority));
    }

    /** {@inheritDoc} */
    public final FileAnnotation getAnnotation(final long key) {
        FileAnnotation annotation = annotations.get(key);
        if (annotation != null) {
            return annotation;
        }
        throw new NoSuchElementException("Annotation not found: key=" + key);
    }

    /** {@inheritDoc} */
    public final FileAnnotation getAnnotation(final String key) {
        return getAnnotation(Long.parseLong(key));
    }

    /**
     * Returns a tooltip showing the distribution of priorities for this container.
     *
     * @return a tooltip showing the distribution of priorities
     */
    public String getToolTip() {
        StringBuilder message = new StringBuilder();
        for (Priority priority : Priority.values()) {
            if (hasAnnotations(priority)) {
                message.append(priority.getLocalizedString());
                message.append(":");
                message.append(getNumberOfAnnotations(priority));
                message.append(" - ");
            }
        }
        return StringUtils.removeEnd(message.toString(), " - ");
    }


    /**
     * Returns the package category name for the scanned files. Currently, only
     * java and c# files are supported.
     *
     * @return the package category name for the scanned files
     */
    public final String getPackageCategoryName() {
        if (hasAnnotations()) {
            String fileName = getAnnotations().iterator().next().getFileName();
            if (fileName.endsWith(".cs")) {
                return Messages.NamespaceDetail_header();
            }
        }
        return Messages.PackageDetail_header();
    }

    /**
     * Gets the files of this container that have annotations.
     *
     * @return the files with annotations
     */
    @java.lang.SuppressWarnings("unchecked")
    public Collection<WorkspaceFile> getFiles() {
        if (handleFiles) {
            return Collections.unmodifiableCollection(filesByName.values());
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Gets the file with the given name.
     *
     * @param fileName
     *            the short name of the file
     * @return the file with the given name
     */
    public WorkspaceFile getFile(final String fileName) {
        if (handleFiles && filesByName.containsKey(fileName)) {
            return filesByName.get(fileName);
        }
        throw new NoSuchElementException("File not found: " + fileName);
    }
}

