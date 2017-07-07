package hudson.plugins.analysis.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * A base class for {@link Blamer} implementations.
 *
 * @author Lukas Krose
 */
public abstract class AbstractBlamer implements Blamer {
    private final EnvVars environment;
    private final FilePath workspace;
    private TaskListener listener;

    /**
     * Creates a new blamer.
     *
     * @param environment {@link EnvVars environment} of the build
     * @param workspace   workspace of the build
     * @param listener    task listener to print logging statements to
     */
    public AbstractBlamer(final EnvVars environment, final FilePath workspace, final TaskListener listener) {
        this.environment = environment;
        this.workspace = workspace;
        this.listener = listener;
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
                String storedFileName = annotation.getFileName();
                if (pathsByFileName.containsKey(storedFileName)) {
                    BlameRequest blame = pathsByFileName.get(storedFileName);
                    blame.addLineNumber(annotation.getPrimaryLineNumber());
                }
                else {
                    String absoluteFileName = getCanonicalPath(storedFileName);
                    if (absoluteFileName.startsWith(workspacePath)) {
                        String relativeFileName = absoluteFileName.substring(workspacePath.length());
                        if (relativeFileName.startsWith("/") || relativeFileName.startsWith("\\")) {
                            relativeFileName = relativeFileName.substring(1);
                        }
                        pathsByFileName.put(storedFileName, new BlameRequest(relativeFileName, annotation.getPrimaryLineNumber()));
                        log("Preparing blame for %s (workspace = %s, absolute = %s.%n",
                                storedFileName, workspacePath, absoluteFileName);
                    }
                    else {
                        log("Skipping non-workspace file %s (workspace = %s, absolute = %s.%n",
                                storedFileName, workspacePath, absoluteFileName);
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
        listener.getLogger().append("<Git Blamer> " + String.format(message, args));
    }

    /**
     * Prints the specified error message.
     *
     * @param message the message (format string)
     * @param args    the arguments for the message format
     */
    protected final void error(final String message, final Object... args) {
        listener.error("<Git Blamer> " + String.format(message, args));
    }

    protected FilePath getWorkspace() {
        return workspace;
    }

    protected TaskListener getListener() {
        return listener;
    }

    protected EnvVars getEnvironment() {
        return environment;
    }
}
