package core;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for initializing Git-Repos for testing.
 */
public class GitUtils {

    /**
     * Setups a GitRepo with one initial commit.
     *
     * @return a GitRepo
     */
    public static GitRepo setupInitialGitRepository() {
        GitRepo repo = new GitRepo();
        repo.setIdentity("Git SampleRepoRule", "gits@mplereporule");
        repo.commitFileWithMessage("init", "file", "");
        return repo;
    }

    /**
     * Commits multiple files to a given GitRepo.
     *
     * @param repo to add commits
     * @return Map with filename and the commit hash
     */
    public static Map<String, String> commitDifferentFilesToGitRepository(GitRepo repo) {
        Map<String, String> commits = new HashMap<>();

        repo.setIdentity("Git SampleRepoRule", "gits@mplereporule");
        repo.commitFileWithMessage("commit", "Test.java", "public class Test {\n"
                + "    public Test() {\n"
                + "        System.out.println(\"Test\");"
                + "    }\n"
                + "}");
        commits.put("Test", repo.getLastSha1());

        repo.setIdentity("John Doe", "john@doe");
        repo.commitFileWithMessage("commit", "LoremIpsum.java", "public class LoremIpsum {\n"
                + "    public LoremIpsum() {\n"
                + "        Log.log(\"Lorem ipsum dolor sit amet\");"
                + "    }\n"
                + "}");
        commits.put("LoremIpsum", repo.getLastSha1());

        repo.setIdentity("Alice Miller", "alice@miller");
        repo.commitFileWithMessage("commit", "Bob.java", "public class Bob {\n"
                + "    public Bob() {\n"
                + "        Log.log(\"Bob: 'Where are you?'\");"
                + "    }\n"
                + "}");
        commits.put("Bob", repo.getLastSha1());

        return commits;
    }

}
