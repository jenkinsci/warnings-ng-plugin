package io.jenkins.plugins.analysis.core.scm.analyzer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitAmountOfChange {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitAmountOfChange.class);

    public Map<String, Integer> collectResults(Git git, String hashID) throws Exception {
        Map<String, Integer> result = new HashMap<>();

        DiffFormatter diffFormatter = new DiffFormatter(NullOutputStream.INSTANCE);
        diffFormatter.setRepository(git.getRepository());
        diffFormatter.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);

        RevCommit newCommit;
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            newCommit = walk.parseCommit(git.getRepository().resolve(hashID));
        }

        for (DiffEntry diffEntry : getDiffOfCommit(git, newCommit, diffFormatter)) {
            //for (Edit edit : diffFormatter.toFileHeader(diffEntry).toEditList())
            //    LOGGER.debug("entry {}, mode {}",
            //        diffEntry.getNewPath(), edit.toString());
            /**
             * insertion very trivial. Every entry counts 1. No
             * matter if delete, insert or modify. Unrelated to the amount of change.
             * Can be modified later.
             */
            //if (!result.containsKey(diffEntry.getNewPath()))
            //{
            result.put(diffEntry.getNewPath(), diffFormatter.toFileHeader(diffEntry).toEditList().size());
            //}
            //else
            //{
            //    result.put(
            //        diffEntry.getNewPath(),
            //        result.put(diffEntry.getNewPath(), result.get(diffEntry.getNewPath()) + 1));
            //}
        }

        return result;
    }

    //Helper gets the diff as a string.
    private List<DiffEntry> getDiffOfCommit(Git git, RevCommit newCommit, DiffFormatter diffFormatter)
            throws IOException {

        //Get commit that is previous to the current one.
        RevCommit oldCommit = getPrevHash(git, newCommit);
        if (oldCommit == null) {
            LOGGER.debug("Start of repo");
        }
        AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(git, oldCommit);
        AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(git, newCommit);

        List<DiffEntry> diffEntries = diffFormatter.scan(oldTreeIterator, newTreeIterator);
        diffFormatter.close();
        return diffEntries;
    }

    //Helper function to get the previous commit.
    public RevCommit getPrevHash(Git git, RevCommit commit) throws IOException {

        try (RevWalk walk = new RevWalk(git.getRepository())) {
            // Starting point
            walk.markStart(commit);
            int count = 0;
            for (RevCommit rev : walk) {
                // got the previous commit.
                if (count == 1) {
                    return rev;
                }
                count++;
            }
            walk.dispose();
        }
        //Reached end and no previous commits.
        return null;
    }

    //Helper function to get the tree of the changes in a commit. Written by Rüdiger Herrmann
    public AbstractTreeIterator getCanonicalTreeParser(Git git, ObjectId commitId) throws IOException {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            RevCommit commit = walk.parseCommit(commitId);
            ObjectId treeId = commit.getTree().getId();
            try (ObjectReader reader = git.getRepository().newObjectReader()) {
                return new CanonicalTreeParser(null, reader, treeId);
            }
        }
    }

    public void commit_logs(Git git) throws IOException, NoHeadException, GitAPIException {
        List<String> logMessages = new ArrayList<String>();
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder.setGitDir(new File("/path/to/repo" + "/.git"))
                .setMustExist(true).build();
        git = new Git(repo);
        Iterable<RevCommit> log = git.log().call();
        RevCommit previousCommit = null;
        for (RevCommit commit : log) {
            if (previousCommit != null) {
                AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(git, previousCommit);
                AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(git, commit);
                OutputStream outputStream = new ByteArrayOutputStream();
                try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
                    formatter.setRepository(git.getRepository());
                    formatter.format(oldTreeIterator, newTreeIterator);
                }
                String diff = outputStream.toString();
                System.out.println(diff);
            }
            System.out.println("LogCommit: " + commit);
            String logMessage = commit.getFullMessage();
            System.out.println("LogMessage: " + logMessage);
            logMessages.add(logMessage.trim());
            previousCommit = commit;
        }
        git.close();
    }

}
