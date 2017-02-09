package hudson.plugins.analysis.util;

import java.io.File;
import java.io.IOException;
import java.util.*;

import hudson.remoting.VirtualChannel;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;

import hudson.FilePath;
import hudson.model.Run;
import hudson.plugins.analysis.core.BuildResult;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import org.jenkinsci.remoting.RoleChecker;

/**
 * A Class that is able to assign git blames to a build result.
 * Based on the solution by John Gibson.
 *
 * @author Lukas Krose
 */
public class GitBlamer extends AbstractBlamer {

    private final GitSCM scm;

    public GitBlamer(Run<?, ?> run, FilePath workspace, PluginLogger logger) {
        super(run, workspace, logger);
        AbstractProject aProject = (AbstractProject) run.getParent();
        scm = (GitSCM) aProject.getScm();
    }

    private HashMap<String, BlameResult> loadBlameResultsForFiles(HashMap<String, String> pathsByFileName) throws InterruptedException, IOException {
        TaskListener listener = TaskListener.NULL;

        if (!(run instanceof AbstractBuild)) {
            logger.log("Could not get parent git client.");
            return null;
        }
        AbstractBuild aBuild = (AbstractBuild) run;
        final EnvVars environment = run.getEnvironment(listener);
        final String gitCommit = environment.get("GIT_COMMIT");
        final String gitExe = scm.getGitExe(aBuild.getBuiltOn(), listener);

        GitClient git = Git.with(listener, environment)
                .in(workspace)
                .using(gitExe)
                .getClient();

        ObjectId headCommit;
        if ((gitCommit == null) || "".equals(gitCommit)) {
            logger.log("No GIT_COMMIT environment variable found, using HEAD.");
            headCommit = git.revParse("HEAD");
        }
        else {
            headCommit = git.revParse(gitCommit);
        }

        if (headCommit == null) {
            logger.log("Could not retrieve HEAD commit.");
            return null;
        }

        HashMap<String, BlameResult> blameResults = new HashMap<String, BlameResult>();
        for (final String child : pathsByFileName.values()) {
            if (BAD_PATH.equals(child)) {
                continue;
            }
            BlameCommand blame = new BlameCommand(git.getRepository());
            blame.setFilePath(child);
            blame.setStartCommit(headCommit);
            try {
                BlameResult result = blame.call();
                if (result == null) {
                    logger.log("No blame results for file: " + child);
                }
                blameResults.put(child, result);
                if (Thread.interrupted()) {
                    throw new InterruptedException("Thread was interrupted while computing blame information.");
                }
            }
            catch (GitAPIException e) {
                final IOException e2 = new IOException("Error running git blame on " + child + " with revision: " + headCommit); // NOPMD: false positive, the exception is used as the cause of the reported error
                e2.initCause(e);
                throw e2;  // NOPMD: false positive
            }
        }

        return blameResults;
    }

    /**
     * Assigns the BlameResults to the annotations.
     *
     * @param annotations The annotations that should be blamed
     * @param pathsByFileName the annotations by filename
     * @param blameResults the results of the blaming
     */
    private void assignBlameResults(final Set<FileAnnotation> annotations,final HashMap<String, String> pathsByFileName, final HashMap<String, BlameResult> blameResults)
    {
        HashSet<String> missingBlame = new HashSet<String>();
        for (final FileAnnotation annot : annotations) {
            if (annot.getPrimaryLineNumber() <= 0) {
                continue;
            }
            String child = pathsByFileName.get(annot.getFileName());
            if (BAD_PATH.equals(child)) {
                continue;
            }
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
                logger.log("Blame details were out of bounds for line number " + annot.getPrimaryLineNumber() + " in file " + child);
            }
        }

        if (!missingBlame.isEmpty()) {
            ArrayList<String> l = new ArrayList<String>(missingBlame);
            Collections.sort(l);
            for (final String child : l) {
                logger.log("Blame details were incomplete for file: " + child);
            }
        }
    }

    @Override
    public void blame(final Set<FileAnnotation> annotations) {
        logger.log("Adding authors to annotations");
        if (annotations.isEmpty()) {
            return;
        }
        try {
            final HashMap<String, String> filePathsByName = getFilePathsFromAnnotations(annotations);

            workspace.act(new FilePath.FileCallable<Void>() {
                @Override
                public void checkRoles(RoleChecker roleChecker) throws SecurityException {
                }

                public Void invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                    HashMap<String, BlameResult> blameResults = loadBlameResultsForFiles(filePathsByName);
                    assignBlameResults(annotations, filePathsByName, blameResults);
                    return null;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
