package hudson.plugins.analysis.util;

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
    /** The PluginLogger to log ,essages */
    private final PluginLogger logger;

    /**
     * Creates a new {@link AbstractBlamer}.
     *
     * @param build     the build
     * @param workspace the workspace of the build
     * @param listener
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
        String workspacePath = workspace.getRemote().replace('\\', '/');
        Map<String, BlameRequest> pathsByFileName = new HashMap<String, BlameRequest>();

        for (FileAnnotation annotation : annotations) {
            if (annotation.getPrimaryLineNumber() > 0) {
                String absoluteFileName = annotation.getFileName();
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
                        log("Skipping non-workspace file " + annotation.getFileName());
                    }
                }
            }
        }
        return pathsByFileName;
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
