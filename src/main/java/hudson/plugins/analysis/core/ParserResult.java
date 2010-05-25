package hudson.plugins.analysis.core;

import java.io.File;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import hudson.FilePath;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.FileFinder;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import java.util.TreeSet;

/**
 * Stores the collection of parsed annotations and associated error messages.
 *
 * @author Ulli Hafner
 */
public class ParserResult implements Serializable {
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ParserResult.class.getName());
    /** Unique ID of this class. */
    private static final long serialVersionUID = -8414545334379193330L;
    /** The parsed annotations. */
    @SuppressWarnings("Se")
    private final Set<FileAnnotation> annotations = new HashSet<FileAnnotation>();
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
    /** A mapping of relative file names to absolute file names. */
    @SuppressWarnings("Se")
    private final Map<String, String> fileNameCache = new HashMap<String, String>();

    /**
     * Creates a new instance of {@link ParserResult}.
     */
    public ParserResult() {
        this((FilePath)null);
    }

    /**
     * Creates a new instance of {@link ParserResult}.
     *
     * @param workspace
     *            the workspace to find the files in
     */
    public ParserResult(final FilePath workspace) {
        this.workspace = workspace;

        Priority[] priorities = Priority.values();

        for (int priority = 0; priority < priorities.length; priority++) {
            annotationCountByPriority.put(priorities[priority], 0);
        }
    }


    /**
     * Creates a new instance of {@link ParserResult}.
     *
     * @param annotations
     *            the annotations to add
     */
    public ParserResult(final Collection<FileAnnotation> annotations) {
        this((FilePath)null);

        addAnnotations(annotations);
    }

    /**
     * Finds a file with relative filename and replaces the name with the absolute path.
     *
     * @param annotation the annotation
     */
    private void expandRelativePaths(final FileAnnotation annotation) {
        try {
            if (workspace != null && hasRelativeFileName(annotation)) {
                FilePath remote = workspace.child(annotation.getFileName());
                if (remote.exists()) {
                    annotation.setFileName(remote.getRemote());
                }
                else {
                    findFileByScanningAllWorkspaceFiles(annotation);
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
     * Returns the file name from the cache of all workspace files. The cache will
     * be built only once.
     *
     * @param annotation
     *            the annotation to get the filename for
     * @throws IOException
     *             signals that an I/O exception has occurred.
     * @throws InterruptedException
     *             If the user cancels this action
     */
    private void findFileByScanningAllWorkspaceFiles(final FileAnnotation annotation) throws IOException, InterruptedException {
        if (fileNameCache.isEmpty()) {
            populateFileNameCache();
        }

        if (fileNameCache.containsKey(annotation.getFileName())) {
            annotation.setFileName(workspace.getRemote() + "/" + fileNameCache.get(annotation.getFileName()));
        }
    }

    /**
     * Builds a cache of file names in the remote file system.
     *
     * @throws IOException
     *             if the file could not be read
     * @throws InterruptedException
     *             if the user cancels the search
     */
    private void populateFileNameCache() throws IOException, InterruptedException {
        LOGGER.log(Level.INFO, "Building cache of all workspace files to obtain absolute filenames for all warnings.");

        String[] allFiles = workspace.act(new FileFinder("**/*"));
        Set<String> duplicates = new TreeSet<String>();
        for (String file : allFiles) {
            String fileName = new File(file).getName();
            if (fileNameCache.containsKey(fileName)) {
                if (duplicates.size() >= 20) {
                    duplicates.add("\u2026"); // HORIZONTAL ELLIPSIS sorts after ASCII whereas ... FULL STOP is before letters
                } else {
                    duplicates.add(fileName);
                }
                fileNameCache.remove(fileName);
            }
            else {
                fileNameCache.put(fileName, file);
            }
        }
        if (!duplicates.isEmpty()) {
            LOGGER.log(Level.INFO, "Relative filenames {0} found more than once; absolute filename resolution disabled for these files.", duplicates);
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
    public final void addAnnotation(final FileAnnotation annotation) {
        if (!annotations.contains(annotation)) {
            expandRelativePaths(annotation);

            annotations.add(annotation);
            Integer count = annotationCountByPriority.get(annotation.getPriority());
            annotationCountByPriority.put(annotation.getPriority(), count + 1);
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
     * Addds an error message for the specified module name.
     *
     * @param module
     *            the current module
     * @param message
     *            the error message
     */
    public void addErrorMessage(final String module, final String message) {
        errorMessages.add(Messages.Result_Error_ModuleErrorMessage(module, message));
    }

    /**
     * Adds an error message.
     *
     * @param message
     *            the error message
     */
    public void addErrorMessage(final String message) {
        errorMessages.add(message);
    }

    /**
     * Adds the error messages to this result.
     *
     * @param errors the error messages to add
     */
    public void addErrors(final List<String> errors) {
        errorMessages.addAll(errors);
    }

    /**
     * Returns the errorMessages.
     *
     * @return the errorMessages
     */
    public List<String> getErrorMessages() {
        return ImmutableList.copyOf(errorMessages);
    }

    /**
     * Returns the annotations of this result.
     *
     * @return the annotations of this result
     */
    public Set<FileAnnotation> getAnnotations() {
        return ImmutableSet.copyOf(annotations);
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getNumberOfAnnotations() + " annotations";
    }
}

