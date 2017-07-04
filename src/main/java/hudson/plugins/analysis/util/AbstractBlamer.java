package hudson.plugins.analysis.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * A base class for {@link Blamer} implementations.
 *
 * @author Lukas Krose
 */
public abstract class AbstractBlamer implements Blamer {
    /** The current run */
    private final AbstractBuild<?, ?> build;
    /** the current workspace */
    private final FilePath workspace;
    private TaskListener listener;
    /** The PluginLogger to log messages */
    private final PluginLogger logger;

    /**
     * Creates a new {@link AbstractBlamer}.
     *
     * @param build     the build
     * @param workspace the workspace of the build
     * @param listener  task listener to print git logging statements to
     * @param logger    the plugin logger
     */
    public AbstractBlamer(final AbstractBuild<?, ?> build, final FilePath workspace, final TaskListener listener, final PluginLogger logger) {
        this.build = build;
        this.workspace = workspace;
        this.listener = listener;
        this.logger = logger;
    }

    /**
     * Extracts the relative file names of the files that contain annotations
     * to make sure every file is blamed only once.
     *
     * @param annotations the annotations to extract the file names from
     * @return a mapping of absolute to relative file names of the conflicting files
     */
    protected Map<String, BlameRequest> extractConflictingFiles(final Set<FileAnnotation> annotations) {
        Map<String, BlameRequest> pathsByFileName = new HashMap<String, BlameRequest>();

        String workspacePath = getWorkspacePath();
        for (FileAnnotation annotation : annotations) {
            if (annotation.getPrimaryLineNumber() > 0) {
                String absoluteFileName = getCanonicalPath(annotation.getFileName());
                if (pathsByFileName.containsKey(absoluteFileName)) {
                    BlameRequest blame = pathsByFileName.get(absoluteFileName);
                    blame.addLineNumber(annotation.getPrimaryLineNumber());
                }
                else {
                    if (absoluteFileName.startsWith(workspacePath)) {
                        String relativeFileName = annotation.getFileName().substring(workspacePath.length());
                        if (relativeFileName.startsWith("/") || relativeFileName.startsWith("\\")) {
                            relativeFileName = relativeFileName.substring(1);
                        }
                        pathsByFileName.put(absoluteFileName, new BlameRequest(relativeFileName, annotation.getPrimaryLineNumber()));
                    }
                    else {
                        log("Skipping non-workspace file %s (workspace = %s, absolute = %s)",
                                annotation.getFileName(), workspacePath, absoluteFileName);
                    }
                }
            }
        }
        return pathsByFileName;
    }

    private String getWorkspacePath() {
        return getCanonicalPath(workspace.getRemote().replace('\\', '/'));
    }

    private String getCanonicalPath(final String path) {
        try {
            return new File(path).getCanonicalPath();
        }
        catch (IOException e) {
            return path;
        }
    }

    /**
     * Logs the specified message.
     *
     * @param message the message (format string)
     * @param args    the arguments for the message format
     */
    protected final void log(final String message, final Object... args) {
        logger.log("<Git Blamer> " + String.format(message, args));
    }

    protected FilePath getWorkspace() {
        return workspace;
    }

    protected AbstractBuild<?, ?> getBuild() {
        return build;
    }

    protected TaskListener getListener() {
        return listener;
    }
}
