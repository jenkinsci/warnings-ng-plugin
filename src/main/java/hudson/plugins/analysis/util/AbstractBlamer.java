package hudson.plugins.analysis.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import hudson.FilePath;
import hudson.model.Run;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * A base class for blame functionality.
 *
 * @author Lukas Krose
 */
public abstract class AbstractBlamer implements Blamer {
    /** The current run */
    private final Run<?, ?> run;
    /** the current workspace */
    private final FilePath workspace;
    /** The PluginLogger to log ,essages */
    private final PluginLogger logger;

    /**
     * Creates a new {@link AbstractBlamer}.
     *
     * @param run       the run
     * @param workspace the workspace of the run
     * @param logger    the plugin logger
     */
    public AbstractBlamer(final Run<?, ?> run, final FilePath workspace, final PluginLogger logger) {
        this.run = run;
        this.workspace = workspace;
        this.logger = logger;
    }

    /**
     * Extracts the relative file names of the files that contain annotations
     * to make sure every file is blamed only once.
     *
     * @param annotations the annotations to extract the file names from
     * @return a set of relative file names
     */
    protected Map<String, String> getFilePathsFromAnnotations(final Set<FileAnnotation> annotations) {
        String workspacePath = workspace.getRemote().replace('\\', '/');
        Map<String, String> pathsByFileName = new HashMap<String, String>();

        for (FileAnnotation annotation : annotations) {
            String absoluteFileName = annotation.getFileName();
            if (!pathsByFileName.containsKey(absoluteFileName) && annotation.getPrimaryLineNumber() > 0) {
                if (absoluteFileName.startsWith(workspacePath)) {
                    String relativeFileName = annotation.getFileName().substring(workspacePath.length());
                    if (relativeFileName.startsWith("/") || relativeFileName.startsWith("\\")) {
                        relativeFileName = relativeFileName.substring(1);
                    }
                    pathsByFileName.put(absoluteFileName, relativeFileName);
                }
                else {
                    log("Skipping non-workspace file " + annotation.getFileName());
                }
            }
        }
        return pathsByFileName;
    }

    /**
     * Logs the specified message.
     *
     * @param message the message
     */
    protected void log(final String message) {
        logger.log(message);
    }

    protected FilePath getWorkspace() {
        return workspace;
    }

    public Run<?, ?> getRun() {
        return run;
    }
}
