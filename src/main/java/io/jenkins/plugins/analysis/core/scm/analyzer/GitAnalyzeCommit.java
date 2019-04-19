package io.jenkins.plugins.analysis.core.scm.analyzer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitAnalyzeCommit {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitLogsPerFile.class);

    public Map<String, Map<String, List<RevCommit>>> collectResults(Git git) throws Exception {
        Map result = new HashMap();
        LOGGER.debug("Analyze: GitAnalyzeCommit");
        List<Path> files = Files.walk(Paths.get(git.getRepository().getWorkTree().getPath()))
                .filter(path -> Files.isRegularFile(path))
                .collect(Collectors.toList());
        for (Path path : files) {
            String relative = new File(git.getRepository().getWorkTree().getAbsolutePath()).toURI()
                    .relativize(new File(path.toString()).toURI())
                    .getPath();
            Iterable<RevCommit> logs = git.log()
                    .addPath(relative)
                    .call();
            result.put(relative, analyzeCommitPerUser(logs));
        }
        return result;
    }

    public Map<String, ArrayList<RevCommit>> analyzeCommitPerUser(Iterable<RevCommit> logs) {
        Map<String, ArrayList<RevCommit>> commitsPerUser = new HashMap<>();
        for (RevCommit rev : logs) {
            if (!commitsPerUser.containsKey(rev.getCommitterIdent().getEmailAddress())) {
                ArrayList<RevCommit> tempList = new ArrayList<>();
                tempList.add(rev);
                commitsPerUser.put(rev.getCommitterIdent().getEmailAddress(), tempList);
            }
            else {
                ArrayList<RevCommit> tempList = commitsPerUser.get(rev.getCommitterIdent().getEmailAddress());
                tempList.add(rev);
                commitsPerUser.put(rev.getCommitterIdent().getEmailAddress(), tempList);
            }
        }
        return commitsPerUser;
    }
}
