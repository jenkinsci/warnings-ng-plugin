package hudson.plugins.warnings.util;

import hudson.FilePath;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.DirectoryScanner;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Stores the collection of parsed annotations and associated error messages.
 *
 * @author Ulli Hafner
 */
public class ParserResult implements Serializable {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -8414545334379193330L;
    /** The parsed annotations. */
    @SuppressWarnings("Se")
    private final Set<FileAnnotation> annotations = new HashSet<FileAnnotation>();
    /** Filter for ant file-set pattern of files to exclude from report. */
    @SuppressWarnings("Se")
    private final transient ExcludeFilter excludeFilter;
    /** The collection of error messages. */
    @SuppressWarnings("Se")
    private final List<String> errorMessages = new ArrayList<String>();
    /** Number of annotations by priority. */
    @SuppressWarnings("Se")
    private final Map<Priority, Integer> annotationCountByPriority = new HashMap<Priority, Integer>();
    /** The set of modules. */
    @SuppressWarnings("Se")
    private final Set<String> modules = new HashSet<String>();
    /** The workspace (might be null). */
    private final FilePath workspace;

    /**
     * Creates a new instance of {@link ParserResult}.
     */
    public ParserResult() {
        this(null, null);
    }

    /**
     * Creates a new instance of {@link ParserResult}.
     *
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report
     */
    public ParserResult(final String excludePattern) {
        this(null, excludePattern);
    }

    /**
     * Creates a new instance of {@link ParserResult}.
     *
     * @param workspace
     *            the workspace to find the files in
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report
     */
    public ParserResult(final FilePath workspace, final String excludePattern) {
        this.workspace = workspace;
        excludeFilter = excludePattern == null ? null : new ExcludeFilter(excludePattern);

        Priority[] priorities = Priority.values();

        for (int priority = 0; priority < priorities.length; priority++) {
            annotationCountByPriority.put(priorities[priority], 0);
        }
    }

    /**
     * Finds a file with relative filename and replaces the name with the absolute path.
     *
     * @param annotation the annotation
     */
    // TODO: maybe we should cache the file names
    public void findRelativeFile(final FileAnnotation annotation) {
        try {
            if (workspace != null && hasRelativeFileName(annotation)) {
                String[] results;
                results = workspace.act(new FileFinder("**/" + annotation.getFileName()));
                if (results.length == 1) {
                    annotation.setFileName(workspace.getRemote() + "/" + results[0]);
                }
            }
        }
        catch (IOException exception) {
            // ignore
        }
        catch (InterruptedException exception) {
            // ignore
        }
    }

    /**
     * Returns whether the annotation references a relative filename.
     *
     * @param annotation the annotation
     * @return <code>true</code> if the filename is relative
     */
    private boolean hasRelativeFileName(final FileAnnotation annotation) {
        String fileName = annotation.getFileName();
        return !fileName.startsWith("/") && !fileName.contains(":");
    }

    /**
     * Adds the specified annotation to this container.
     *
     * @param annotation the annotation to add
     */
    public void addAnnotation(final FileAnnotation annotation) {
        if (!annotations.contains(annotation)) {
            findRelativeFile(annotation);

            if (excludeFilter == null || !excludeFilter.matches(annotation.getFileName())) {
                annotations.add(annotation);
                Integer count = annotationCountByPriority.get(annotation.getPriority());
                annotationCountByPriority.put(annotation.getPriority(), count + 1);
            }
        }
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations the annotations to add
     */
    public final void addAnnotations(final Collection<? extends FileAnnotation> newAnnotations) {
        for (FileAnnotation annotation : newAnnotations) {
            addAnnotation(annotation);
        }
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations the annotations to add
     */
    public final void addAnnotations(final FileAnnotation[] newAnnotations) {
        addAnnotations(Arrays.asList(newAnnotations));
    }

    /**
     * Sets an error message for the specified module name.
     *
     * @param message
     *            the error message
     */
    public void addErrorMessage(final String message) {
        errorMessages.add(message);
    }

    /**
     * Returns the errorMessages.
     *
     * @return the errorMessages
     */
    public Collection<String> getErrorMessages() {
        return Collections.unmodifiableCollection(errorMessages);
    }

    /**
     * Returns the annotations of this result.
     *
     * @return the annotations of this result
     */
    public Collection<FileAnnotation> getAnnotations() {
        return Collections.unmodifiableCollection(annotations);
    }

    /**
     * Returns the total number of annotations for this object.
     *
     * @return total number of annotations for this object
     */
    public int getNumberOfAnnotations() {
        return annotations.size();
    }

    /**
     * Returns the total number of annotations of the specified priority for
     * this object.
     *
     * @param priority
     *            the priority
     * @return total number of annotations of the specified priority for this
     *         object
     */
    public int getNumberOfAnnotations(final Priority priority) {
        return annotationCountByPriority.get(priority);
    }

    /**
     * Returns whether this objects has annotations.
     *
     * @return <code>true</code> if this objects has annotations.
     */
    public boolean hasAnnotations() {
        return !annotations.isEmpty();
    }

    /**
     * Returns whether this objects has annotations with the specified priority.
     *
     * @param priority
     *            the priority
     * @return <code>true</code> if this objects has annotations.
     */
    public boolean hasAnnotations(final Priority priority) {
        return annotationCountByPriority.get(priority) > 0;
    }

    /**
     * Returns whether this objects has no annotations.
     *
     * @return <code>true</code> if this objects has no annotations.
     */
    public boolean hasNoAnnotations() {
        return !hasAnnotations();
    }

    /**
     * Returns whether this objects has no annotations with the specified priority.
     *
     * @param priority
     *            the priority
     * @return <code>true</code> if this objects has no annotations.
     */
    public boolean hasNoAnnotations(final Priority priority) {
        return !hasAnnotations(priority);
    }

    /**
     * Returns the number of modules.
     *
     * @return the number of modules
     */
    public int getNumberOfModules() {
        return modules.size();
    }

    /**
     * Returns the parsed modules.
     *
     * @return the parsed modules
     */
    public Set<String> getModules() {
        return Collections.unmodifiableSet(modules);
    }

    /**
     * Adds a new parsed module.
     *
     * @param moduleName
     *            the name of the parsed module
     */
    public void addModule(final String moduleName) {
        modules.add(moduleName);
    }

    /**
     * Adds the specified parsed modules.
     *
     * @param additionalModules
     *            the name of the parsed modules
     */
    public void addModules(final Collection<String> additionalModules) {
        modules.addAll(additionalModules);
    }

    /**
     * Filters file names based on Ant file-set patterns.
     */
    private static final class ExcludeFilter extends DirectoryScanner {
        /**
         * Creates a new instance of {@link ExcludeFilter}.
         *
         * @param excludePattern
         *            Ant file-set pattern of files to exclude from report
         */
        public ExcludeFilter(final String excludePattern) {
            super();

            setExcludes(excludePattern.split(",\\s*"));
            setIncludes(new String[] {"**/*"});
        }

        /**
         * Returns whether the name matches one of the exclusion patterns.
         *
         * @param name
         *            the file name to test
         * @return <code>true</code> if the name matches one of the exclusion patterns.
         */
        public boolean matches(final String name) {
            return isExcluded(name);
        }
    }
}

