package hudson.plugins.analysis.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

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
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;

public class AuthorAssigner {
    private static final String BAD_PATH = "/";

    public static void addAuthorToAnnotation(final Run<?, ?> build, final FilePath workspace, BuildResult project, final PluginLogger logger)
            throws IOException, InterruptedException
    {
        if (project.getAnnotations().isEmpty()) {
            return;
        }
        logger.log("Adding authors to annotations");
        if(!(build.getParent() instanceof AbstractProject)) {
            return;
        }
        AbstractProject aProject = (AbstractProject) build.getParent();
        SCM scm = aProject.getScm();
        TaskListener listener = TaskListener.NULL;

        if (!(scm instanceof GitSCM)) {
            logger.log("Only git supported: " + scm);
            return;
        }

        final EnvVars environment = build.getEnvironment(listener);
        final String gitCommit = environment.get("GIT_COMMIT");
        GitSCM gscm = (GitSCM) scm;

        if (!(build instanceof AbstractBuild)) {
            logger.log("Could not get parent git client.");
            return;
        }
        AbstractBuild aBuild = (AbstractBuild) build;
        final String gitExe = gscm.getGitExe(aBuild.getBuiltOn(), listener);

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
            return;
        }

        File workspaceFile = new File(workspace.toURI());
        final String absoluteWorkspace = workspaceFile.getAbsolutePath();
        HashMap<String, String> nameMap = new HashMap<String, String>();
        for (final FileAnnotation annot : project.getAnnotations()) {
            if (nameMap.containsKey(annot.getFileName())) {
                continue;
            }
            if (annot.getPrimaryLineNumber() <= 0) {
                continue;
            }
            String filename = annot.getFileName().replace("/", "\\");
            if (!filename.startsWith(absoluteWorkspace)) {
                logger.log("Saw a file outside of the workspace? " + annot.getFileName());
                nameMap.put(annot.getFileName(), BAD_PATH);
                continue;
            }
            String child = annot.getFileName().substring(absoluteWorkspace.length());
            if (child.startsWith("/") || child.startsWith("\\")) {
                child = child.substring(1);
            }
            nameMap.put(annot.getFileName(), child);
        }

        HashMap<String, BlameResult> blameResults = new HashMap<String, BlameResult>();
        for (final String child : nameMap.values()) {
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

        HashSet<String> missingBlame = new HashSet<String>();
        for (final FileAnnotation annot : project.getAnnotations()) {
            if (annot.getPrimaryLineNumber() <= 0) {
                continue;
            }
            String child = nameMap.get(annot.getFileName());
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
}
