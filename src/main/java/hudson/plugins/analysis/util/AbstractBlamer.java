package hudson.plugins.analysis.util;

import hudson.FilePath;
import hudson.model.Run;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.scm.SCM;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A base class for blame functionality.
 *
 * @author Lukas Krose
 */
public abstract class AbstractBlamer implements BlameInterface{
    /** Indicator for a bad Path */
    protected static final String BAD_PATH = "/";
    /** The current run */
    protected final Run <?, ?> run;
    /** the current workspace */
    protected final FilePath workspace;
    /** The PluginLogger to log ,essages */
    protected final PluginLogger logger;

    public AbstractBlamer(final Run<?, ?> run, final FilePath workspace, final PluginLogger logger)
    {
        this.run = run;
        this.workspace = workspace;
        this.logger = logger;
    }

    /**
     * Sorts the annotations by FileName and Path to make sure every file is only blamed once.
     * Also applies a bad path when the file is outside of the working directory.
     *
     * @param annotations The Set of annotations
     * @return A map of <Filename, FilePath>
     * @throws IOException
     * @throws InterruptedException
     */
    protected HashMap<String, String> getFilePathsFromAnnotations(Set<FileAnnotation> annotations) throws IOException, InterruptedException {
        File workspaceFile = new File(workspace.toURI());
        final String absoluteWorkspace = workspaceFile.getAbsolutePath();
        HashMap<String, String> pathsByFileName = new HashMap<String, String>();
        for (final FileAnnotation annot : annotations) {
            if (pathsByFileName.containsKey(annot.getFileName())) {
                continue;
            }
            if (annot.getPrimaryLineNumber() <= 0) {
                continue;
            }
            String filename = annot.getFileName().replace("/", "\\");
            if (!filename.startsWith(absoluteWorkspace)) {
                logger.log("Saw a file outside of the workspace? " + annot.getFileName());
                pathsByFileName.put(annot.getFileName(), BAD_PATH);
                continue;
            }
            String child = annot.getFileName().substring(absoluteWorkspace.length());
            if (child.startsWith("/") || child.startsWith("\\")) {
                child = child.substring(1);
            }
            pathsByFileName.put(annot.getFileName(), child);
        }
        return pathsByFileName;
    }




}
