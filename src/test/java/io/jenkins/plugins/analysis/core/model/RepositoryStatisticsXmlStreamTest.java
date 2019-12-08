package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.ResourceTest;

import io.jenkins.plugins.forensics.miner.FileStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link RepositoryStatisticsXmlStream}.
 *
 * @author Ullrich Hafner
 */
class RepositoryStatisticsXmlStreamTest extends ResourceTest {
    private static final String FILE = "/path/to/file.txt";
    private static final int ONE_DAY = 60 * 60 * 24;

    @Test
    void shouldWriteReport() {
        RepositoryStatistics statistics = new RepositoryStatistics();
        FileStatistics fileStatistics = new FileStatistics(FILE);
        fileStatistics.inspectCommit(ONE_DAY * 4, "name");
        fileStatistics.inspectCommit(ONE_DAY * 3, "another");
        fileStatistics.inspectCommit(ONE_DAY * 2, "another");
        statistics.add(fileStatistics);

        RepositoryStatisticsXmlStream stream = new RepositoryStatisticsXmlStream();
        Path path = createTempFile();
        stream.write(path, statistics);

        RepositoryStatistics restored = stream.read(path);

        assertThat(restored).hasFiles(FILE);
        FileStatistics restoredFile = restored.get(FILE);
        assertThat(restoredFile).hasNumberOfAuthors(2);
        assertThat(restoredFile).hasNumberOfCommits(3);
        assertThat(restoredFile).hasCreationTime(ONE_DAY * 2);
        assertThat(restoredFile).hasLastModificationTime(ONE_DAY * 4);
    }

    private Path createTempFile() {
        try {
            return Files.createTempFile("test", ".xml");
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
