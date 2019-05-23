package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import org.junit.ClassRule;
import org.junit.Test;

import jenkins.plugins.git.GitSampleRepoRule;

public class GitITest {
    @ClassRule
    public static GitSampleRepoRule repository = new GitSampleRepoRule();

    @Test
    public void shouldCreateRepositoryWithTwoContributors() throws Exception {
        String fileName = "README.md";

        repository.init();
        repository.git("config", "user.name", "Hans Hamburg");
        repository.git("config", "user.email", "hans@hamburg.com");
        repository.git("checkout", "master");
        repository.write(fileName, "Hello!\n");
        repository.git("add", fileName);
        repository.git("commit", "-m", "Initial " + fileName, fileName);

        repository.git("config", "user.name", "Peter Petersburg");
        repository.git("config", "user.email", "peter@petersburg.com");

        File file = new File(repository.getRoot(), fileName);
        FileWriter fr = new FileWriter(file, true);
        fr.write("Bye!");
        fr.close();

        repository.git("add", fileName);
        repository.git("commit", "-m", "Adding to " + fileName, fileName);
        repository.git("blame", fileName);
    }
}
