package hudson.plugins.analysis.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.remoting.RoleChecker;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.git.GitSCM;
import hudson.remoting.VirtualChannel;

/**
 * A Class that is able to assign git blames to a build result.
 * Based on the solution by John Gibson.
 *
 * @author Lukas Krose
 */
public class GitBlamer extends AbstractBlamer {
    private final GitSCM scm;
    private final TaskListener listener;

    public GitBlamer(Run<?, ?> run, FilePath workspace, PluginLogger logger, final TaskListener listener) {
        super(run, workspace, logger);

        this.listener = listener;
        AbstractProject aProject = (AbstractProject) run.getParent();
        scm = (GitSCM) aProject.getScm();
    }

    private Map<String, BlameResult> loadBlameResultsForFiles(Map<String, String> pathsByFileName) throws InterruptedException, IOException {
        if (!(getRun() instanceof AbstractBuild)) {
            log("Could not get parent git client.");
            return null;
        }
        AbstractBuild aBuild = (AbstractBuild) getRun();
        final EnvVars environment = getRun().getEnvironment(listener);
        final String gitCommit = environment.get("GIT_COMMIT");
        final String gitExe = scm.getGitExe(aBuild.getBuiltOn(), listener);

        GitClient git = Git.with(listener, environment).in(getWorkspace()).using(gitExe).getClient();

        ObjectId headCommit;
        if ((gitCommit == null) || "".equals(gitCommit)) {
            log("No GIT_COMMIT environment variable found, using HEAD.");
            headCommit = git.revParse("HEAD");
        }
        else {
            headCommit = git.revParse(gitCommit);
        }

        if (headCommit == null) {
            log("Could not retrieve HEAD commit.");
            return null;
        }

        HashMap<String, BlameResult> blameResults = new HashMap<String, BlameResult>();
        for (final String fileName : pathsByFileName.values()) {
            BlameCommand blame = new BlameCommand(git.getRepository());
            blame.setFilePath(fileName);
            blame.setStartCommit(headCommit);
            try {
                BlameResult result = blame.call();
                if (result == null) {
                    log("No blame results for file: " + fileName);
                }
                blameResults.put(fileName, result);
                if (Thread.interrupted()) {
                    throw new InterruptedException("Thread was interrupted while computing blame information.");
                }
            }
            catch (GitAPIException e) {
                throw new IOException("Error running git blame on " + fileName + " with revision: " + headCommit, e);
            }
        }

        return blameResults;
    }

    /**
     * Assigns the BlameResults to the annotations.
     *
     * @param annotations  The annotations that should be blamed
     * @param pathsByFileName        the filenames of the conflicting files
     * @param blameResults the results of the blaming
     */
    private void assignBlameResults(final Set<FileAnnotation> annotations,
            final Map<String, String> pathsByFileName, final Map<String, BlameResult> blameResults) {
        HashSet<String> missingBlame = new HashSet<String>();
        for (final FileAnnotation annot : annotations) {
            if (annot.getPrimaryLineNumber() <= 0) {
                continue;
            }
            String child = pathsByFileName.get(annot.getFileName());
            BlameResult blame = blameResults.get(child);
            if (blame == null) {
                continue;
            }
            int zeroline = annot.getPrimaryLineNumber() - 1;
            try {
                PersonIdent who = blame.getSourceAuthor(zeroline);
                RevCommit commit = blame.getSourceCommit(zeroline);
                if (who == null) {
                    missingBlame.add(child);
                }
                else {
                    annot.setAuthorName(who.getName());
                    annot.setAuthorEmail(who.getEmailAddress());
                }
                annot.setAuthorCommitId(commit == null ? null : commit.getName());
            }
            catch (ArrayIndexOutOfBoundsException e) {
                log("Blame details were out of bounds for line number " + annot.getPrimaryLineNumber() + " in file " + child);
            }
        }

        if (!missingBlame.isEmpty()) {
            ArrayList<String> l = new ArrayList<String>(missingBlame);
            Collections.sort(l);
            for (final String child : l) {
                log("Blame details were incomplete for file: " + child);
            }
        }
    }

    @Override
    public void blame(final Set<FileAnnotation> annotations) {
        log("Mapping annotations to Git commits IDs and authors");
        if (annotations.isEmpty()) {
            return;
        }

        try {
            final Map<String, String> files = getFilePathsFromAnnotations(annotations);

            getWorkspace().act(new FilePath.FileCallable<Void>() {
                @Override
                public void checkRoles(RoleChecker roleChecker) throws SecurityException {
                }

                // FIXME: does not work on slaves, required is a serialization back
                public Void invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                    Map<String, BlameResult> blameResults = loadBlameResultsForFiles(files);
                    assignBlameResults(annotations, files, blameResults);
                    return null;
                }
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
