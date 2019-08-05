package io.jenkins.plugins.analysis.core.scm.analyzer;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitLogsPerFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitLogsPerFile.class);

    public Map<String, Iterable<RevCommit>> collectResults(Git git) throws Exception {
        LOGGER.debug("Analyze: GitLogsPerFile");

        Map result = new HashMap();
        for (Path path : Util.getFilesInRepository(git)) {
            String pathRelative = Util.getFileRelative(git, path);
            Iterable<RevCommit> logs = git.log().addPath(pathRelative).call();
            result.put(pathRelative, logs);
        }
        return result;
    }

}
