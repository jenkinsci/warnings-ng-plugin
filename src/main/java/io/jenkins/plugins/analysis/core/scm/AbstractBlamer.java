package io.jenkins.plugins.analysis.core.scm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import jenkins.MasterToSlaveFileCallable;

import hudson.FilePath;
import hudson.model.TaskListener;
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
     * @param workspace
     *         workspace of the repository
     * @param listener
     *         task listener to print logging statements to
     */
    public AbstractBlamer(final FilePath workspace, final TaskListener listener) {
        this.workspace = workspace;
        this.listener = listener;
    }

    @Override
    public Blames blame(final Report report) {
        try {
            if (!report.isEmpty()) {
                return invokeBlamer(report);
            }
        }
        catch (IOException e) {
            report.logError("Computing blame information failed with an exception:%n%s%n%s",
                    e.getMessage(), ExceptionUtils.getStackTrace(e));
        }
        catch (InterruptedException e) {
            // nothing to do, already logged
        }
        return new Blames();
    }

    private Blames invokeBlamer(final Report report) throws IOException, InterruptedException {
        final Blames blames = extractConflictingFiles(report);

        Blames blamesOfConflictingFiles = getWorkspace().act(
                new MasterToSlaveFileCallable<Blames>() {
                    @Override
                    public Blames invoke(final File workspace, final VirtualChannel channel)
                            throws IOException, InterruptedException {
                        return blameOnAgent(blames);
                    }
                });

        return blamesOfConflictingFiles;
    }

    /**
     * Computes for each conflicting file a {@link BlameRequest}. Note that this call is executed on a build agent.
     * I.e., the blamer instance and all transfer objects need to be serializable.
     *
     * @param linesOfConflictingFiles
     *         a mapping of file names to blame request. Each blame request defines the lines that need to be mapped to
     *         an author.
     *
     * @return the same mapping, now filled with blame information
     * @throws InterruptedException
     *         if this operation has been canceled
     * @throws IOException
     *         in case of an error
     */
    protected abstract Blames blameOnAgent(Blames linesOfConflictingFiles)
            throws InterruptedException, IOException;

    /**
     * Extracts the relative file names of the files that contain annotations to make sure every file is blamed only
     * once.
     *
     * @param report
     *         the issues to extract the file names from
     *
     * @return a mapping of absolute to relative file names of the conflicting files
     */
    protected Blames extractConflictingFiles(final Report report) {
        Blames blames = new Blames();

        String workspacePath = getWorkspacePath();
        List<String> errorLog = new ArrayList<>();

        for (Issue issue : report) {
            if (issue.getLineStart() > 0) {
                String storedFileName = issue.getFileName();
                if (blames.contains(storedFileName)) {
                    blames.addLine(storedFileName, issue.getLineStart());
                }
                else { 
                    String absoluteFileName = getCanonicalPath(storedFileName);
                    if (absoluteFileName.startsWith(workspacePath)) {
                        String relativeFileName = absoluteFileName.substring(workspacePath.length());
                        if (relativeFileName.startsWith("/") || relativeFileName.startsWith("\\")) {
                            relativeFileName = relativeFileName.substring(1);
                        }
                        blames.addRequest(storedFileName,
                                new BlameRequest(relativeFileName, issue.getLineStart()));
                    }
                    else {
                        int error = errorLog.size();
                        if (error < 5) {
                            errorLog.add(String.format(
                                    "Skipping non-workspace file %s (workspace = %s, absolute = %s).%n",
                                    storedFileName, workspacePath, absoluteFileName));
                        }
                        else if (error == 5) {
                            errorLog.add("  ... skipped logging of additional non-workspace file errors ...");
                        }
                    }
                }
            }
        }
        
        if (blames.isEmpty()) {
            report.logError("Created no blame requests - Git blame will be skipped");
            errorLog.forEach(report::logError);
        }
        else {
            report.logInfo("Created blame requests for %d files - invoking Git blame on agent for each of the requests",
                    blames.size());
            errorLog.forEach(report::logError);
        }
        return blames;
    }

    private String getWorkspacePath() {
        return getCanonicalPath(workspace.getRemote());
    }

    private String getCanonicalPath(final String path) {
        try {
            return new File(path).getCanonicalPath().replace('\\', '/');
        }
        catch (IOException e) {
            return path;
        }
    }

    /**
     * Prints the specified error message.
     *
     * @param message
     *         the message (format string)
     * @param args
     *         the arguments for the message format
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
