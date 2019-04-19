package io.jenkins.plugins.analysis.core.scm.analyzer;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

public class GitFileCreated {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitFileCreated.class);

    public Map<String, Integer> collectResults(Git git) throws Exception {
        LOGGER.debug("Analyze: GitFileCreated");
        Map result = new HashMap();
        for (Path path : Util.getFilesInRepository(git)) //for every file we walk over the logs
        {
            String pathRelative = Util.getFileRelative(git, path);
            Iterable<RevCommit> logs = git.log().addPath(pathRelative).call();
            RevCommit revCommit = Iterables.getLast(logs);
            if (revCommit != null) {
                result.put(pathRelative, revCommit.getCommitTime());
            }
        }
        return result;
    }
}
