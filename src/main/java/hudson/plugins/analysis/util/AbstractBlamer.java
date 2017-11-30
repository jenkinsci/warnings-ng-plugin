package hudson.plugins.analysis.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

import org.apache.commons.lang3.exception.ExceptionUtils;

import jenkins.MasterToSlaveFileCallable;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.remoting.VirtualChannel;

/**
 * A base class for {@link Blamer} implementations.
 *
 * @author Lukas Krose
 */
public abstract class AbstractBlamer implements Blamer {
    private final FilePath workspace;
    private final TaskListener listener;

    /**
     * Creates a new blamer.
     *
     * @param workspace workspace of the repository
     * @param listener  task listener to print logging statements to
     */
    public AbstractBlamer(final FilePath workspace, final TaskListener listener) {
        this.workspace = workspace;
        this.listener = listener;
    }

    @Override
    public void blame(final Set<FileAnnotation> annotations) {
        try {
            if (annotations.isEmpty()) {
                return;
            }

            invokeBlamer(annotations);
        }
        catch (IOException e) {
            error("Computing blame information failed with an exception:%n%s%n%s",
                    e.getMessage(), ExceptionUtils.getStackTrace(e));
        }
        catch (InterruptedException e) {
            // nothing to do, already logged
        }
    }

    private void invokeBlamer(final Set<FileAnnotation> annotations) throws IOException, InterruptedException {
        final Map<String, BlameRequest> linesOfConflictingFiles = extractConflictingFiles(annotations);

        Map<String, BlameRequest> blamesOfConflictingFiles = getWorkspace().act(new MasterToSlaveFileCallable<Map<String, BlameRequest>>() {
            @Override
            public Map<String, BlameRequest> invoke(final File workspace, final VirtualChannel channel) throws IOException, InterruptedException {
                return blame(linesOfConflictingFiles);
            }
        });

        setBlameResults(annotations, blamesOfConflictingFiles);
    }

    private void setBlameResults(final Set<FileAnnotation> annotations, final Map<String, BlameRequest> blamesOfConflictingFiles) {
        for (FileAnnotation annotation : annotations) {
            if (blamesOfConflictingFiles.containsKey(annotation.getFileName())) {
                BlameRequest blame = blamesOfConflictingFiles.get(annotation.getFileName());
                int line = annotation.getPrimaryLineNumber();
                annotation.setAuthorName(blame.getName(line));
                annotation.setAuthorEmail(blame.getEmail(line));
                annotation.setCommitId(blame.getCommit(line));
            }
            else {
                log("Skipping file %s, no result found.%n", annotation.getFileName());
            }
        }
    }

    /**
     * Computes for each conflicting file a {@link BlameRequest}. Note that this call is executed on a build agent.
     * I.e., the blamer instance and all transfer objects need to be serializable.
     *
     * @param linesOfConflictingFiles a mapping of file names to blame request. Each blame request defines the lines
     *                                that need to be mapped to an author.
     * @return the same mapping, now filled with blame information
     * @throws InterruptedException if this operation has been canceled
     * @throws IOException          in case of an error
     */
    protected abstract Map<String, BlameRequest> blame(Map<String, BlameRequest> linesOfConflictingFiles) throws InterruptedException, IOException;

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
            String filePath;
            filePath = new File(path).getCanonicalPath();
            filePath = StringUtils.strip(filePath).replace('\\', '/');
            return filePath;
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

    /**
     * Returns the workspace path on the agent.
     *
     * @return workspace path
     */
    protected FilePath getWorkspace() {
        return workspace;
    }

    /**
     * Returns the listener for logging statements.
     *
     * @return logger
     */
    protected TaskListener getListener() {
        return listener;
    }
}
