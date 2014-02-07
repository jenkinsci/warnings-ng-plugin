package hudson.plugins.analysis.core;

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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import hudson.FilePath;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.FileFinder;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Stores the collection of parsed annotations and associated error messages.
 * This class is not thread safe.
 *
 * @author Ulli Hafner
 */
public class ParserResult implements Serializable {
    private static final long serialVersionUID = -8414545334379193330L;
    private static final Logger LOGGER = Logger.getLogger(ParserResult.class.getName());
    private static final String SLASH = "/";

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
    /** The workspace. */
    private final Workspace workspace;
    /** A mapping of relative file names to absolute file names. */
    @SuppressWarnings("Se")
    private final Multimap<String, String> fileNameCache = HashMultimap.create();
    /** The log messages. @since 1.20 **/
    private String logMessage;
    /** Total number of modules. @since 1.31 **/
    private int numberOfModules;
    /**
     * Determines whether relative paths in warnings should be
     * resolved using a time expensive operation that scans the whole
     * workspace for matching files.
     *
     * @since 1.55
     */
    private final boolean canResolveRelativePaths;

    /**
     * Creates a new instance of {@link ParserResult}.
     */
    public ParserResult() {
        this(new NullWorkspace());
    }

    /**
     * Creates a new instance of {@link ParserResult}.
     *
     * @param workspace
     *            the workspace to find the files in
     */
    public ParserResult(final FilePath workspace) {
        this(asWorkspace(workspace));
    }

    /**
     * Creates a new instance of {@link ParserResult}.
     *
     * @param workspace
     *            the workspace to find the files in
     */
    public ParserResult(final Workspace workspace) {
        this(workspace, false);
    }

    /**
     * Creates a new instance of {@link ParserResult}.
     *
     * @param workspace
     *            the workspace to find the files in
     * @param canResolveRelativePaths
     *            determines whether relative paths in warnings should be
     *            resolved using a time expensive operation that scans the whole
     *            workspace for matching files
     */
    public ParserResult(final FilePath workspace, boolean canResolveRelativePaths) {
        this(asWorkspace(workspace), canResolveRelativePaths);
    }

    /**
     * Creates a new instance of {@link ParserResult}.
     *
     * @param workspace
     *            the workspace to find the files in
     * @param canResolveRelativePaths
     *            determines whether relative paths in warnings should be
     *            resolved using a time expensive operation that scans the whole
     *            workspace for matching files
     */
    public ParserResult(final Workspace workspace, final boolean canResolveRelativePaths) {
        this.workspace = workspace;
        this.canResolveRelativePaths = canResolveRelativePaths;

        Priority[] priorities = Priority.values();

        for (int priority = 0; priority < priorities.length; priority++) {
            annotationCountByPriority.put(priorities[priority], 0);
        }
    }

    private static FilePathAdapter asWorkspace(final FilePath workspace) {
        return new FilePathAdapter(workspace);
    }

    /**
     * Creates a new instance of {@link ParserResult}.
     *
     * @param annotations
     *            the annotations to add
     */
    public ParserResult(final Collection<? extends FileAnnotation> annotations) {
        this(new NullWorkspace());

        addAnnotations(annotations);
    }


    /**
     * Adds the warnings of the specified project to this project.
     *
     * @param additionalProject the project to add
     */
    public void addProject(final ParserResult additionalProject) {
        addAnnotations(additionalProject.getAnnotations());
        addErrors(additionalProject.getErrorMessages());
        addModules(additionalProject.getModules());
    }

    /**
     * Finds a file with relative filename and replaces the name with the absolute path.
     *
     * @param annotation the annotation
     */
    // TODO: when used on a slave then for each file a remote call is initiated
    private void expandRelativePaths(final FileAnnotation annotation) {
        try {
            if (hasRelativeFileName(annotation)) {
                Workspace remoteFile = workspace.child(annotation.getFileName());
                if (remoteFile.exists()) {
                    annotation.setFileName(remoteFile.getPath());
                }
                else if (canResolveRelativePaths) {
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

        String fileName = FilenameUtils.getName(annotation.getFileName());
        if (fileNameCache.containsKey(fileName)) {
            int matchesCount = 0;
            String absoluteFileName = null;
            for (String match : fileNameCache.get(fileName)) {
                if (match.contains(annotation.getFileName())) {
                    absoluteFileName = workspace.getPath() + SLASH + match;
                    matchesCount++;
                }
            }
            if (matchesCount == 1) {
                annotation.setFileName(absoluteFileName);
            }
            else {
                LOGGER.log(Level.FINE, String.format(
                        "Absolute filename could not be resolved for: %s. Found multiple matches: %s. ",
                        annotation.getFileName(), fileNameCache.get(fileName)));
            }
        }
        else {
            LOGGER.log(Level.FINE, String.format(
                    "Absolute filename could not be resolved for: %s. No such file in workspace: %s. ",
                    annotation.getFileName(), workspace.getPath()));
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
    // TODO: Maybe the file pattern should be exposed on the UI in order to speed up the scanning, see HUDSON-2927
    private void populateFileNameCache() throws IOException, InterruptedException {
        LOGGER.log(Level.FINE, "Building cache of all workspace files to obtain absolute filenames for all warnings: " + workspace.getPath());

        String[] allFiles = workspace.findFiles("**/*");
        for (String file : allFiles) {
            fileNameCache.put(FilenameUtils.getName(file), FilenameUtils.separatorsToUnix(file));
        }
    }

    /**
     * Returns whether the annotation references a relative filename.
     *
     * @param annotation
     *            the annotation
     * @return <code>true</code> if the filename is relative
     */
    private boolean hasRelativeFileName(final FileAnnotation annotation) {
        String fileName = annotation.getFileName();
        return !fileName.startsWith(SLASH) && !fileName.contains(":");
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
     * Adds an error message for the specified module name.
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
        return numberOfModules;
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

        numberOfModules++;
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

    @Override
    public String toString() {
        return getNumberOfAnnotations() + " annotations";
    }

    /**
     * Sets the log messages of the parsing process.
     *
     * @param message a multiline message
     * @since 1.20
     */
    public void setLog(final String message) {
        logMessage = message;
    }

    /**
     * Returns the log messages of the parsing process.
     *
     * @return the messages
     * @since 1.20
     */
    public String getLogMessages() {
        return StringUtils.defaultString(logMessage);
    }

    /**
     * Facade for the remote workspace.
     */
    interface Workspace extends Serializable {
        Workspace child(String fileName);

        boolean exists() throws InterruptedException, IOException;

        String getPath();

        String[] findFiles(String pattern) throws IOException, InterruptedException;
    }

    /**
     * Default implementation that delegates to a {@link FilePath} instance.
     */
    private static class FilePathAdapter implements Workspace {
        private static final long serialVersionUID = 1976601889843466249L;

        private final FilePath wrapped;

        /**
         * Creates a new instance of {@link FilePathAdapter}.
         *
         * @param workspace
         *            the {@link FilePath} to wrap
         */
        FilePathAdapter(final FilePath workspace) {
            wrapped = workspace;
        }

        /** {@inheritDoc} */
        public Workspace child(final String fileName) {
            return asWorkspace(wrapped.child(fileName));
        }

        /** {@inheritDoc} */
        public boolean exists() throws IOException, InterruptedException {
            return wrapped.exists();
        }

        /** {@inheritDoc} */
        public String getPath() {
            return wrapped.getRemote();
        }

        /** {@inheritDoc} */
        public String[] findFiles(final String pattern) throws IOException, InterruptedException {
            return wrapped.act(new FileFinder(pattern));
        }
    }

    /**
     * Null pattern.
     */
    private static class NullWorkspace implements Workspace {
        private static final long serialVersionUID = 2307259492760554066L;

        /** {@inheritDoc} */
        public Workspace child(final String fileName) {
            return this;
        }

        /** {@inheritDoc} */
        public boolean exists() throws IOException, InterruptedException {
            return false;
        }

        /** {@inheritDoc} */
        public String getPath() {
            return StringUtils.EMPTY;
        }

        /** {@inheritDoc} */
        public String[] findFiles(final String pattern) throws IOException, InterruptedException {
            return new String[0];
        }
    }
}

