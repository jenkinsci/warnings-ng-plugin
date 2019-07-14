package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.ResourceTest;

import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileBlame;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link BlamesXmlStream}.
 *
 * @author Ullrich Hafner
 */
class BlamesXmlStreamTest extends ResourceTest {
    private static final String WORKSPACE = "/var/data/workspace/pipeline-analysis-model/";
    private static final String REPORT_SRC = "src/main/java/edu/hm/hafner/analysis/Report.java";
    private static final String REPORT = WORKSPACE + REPORT_SRC;
    private static final String FILTERED_LOG_SRC = "src/main/java/edu/hm/hafner/analysis/FilteredLog.java";
    private static final String FILTERED_LOG = WORKSPACE + FILTERED_LOG_SRC;

    @Test
    void shouldReadBlames() {
        BlamesXmlStream blamesReader = new BlamesXmlStream();

        Object restored = blamesReader.read(getResourceAsFile("java-blames.xml"));

        Path saved = createTempFile();
        assertThat(restored).isInstanceOfSatisfying(Blames.class,
                blames -> {
                    assertThatBlamesAreCorrect(blames);

                    blamesReader.write(saved, blames);
                });

        Blames newFormat = blamesReader.read(saved);
        assertThatBlamesAreCorrect(newFormat);
    }

    private void assertThatBlamesAreCorrect(final Blames blames) {
        assertThat(blames.getFiles()).contains(REPORT, FILTERED_LOG);

        FileBlame report = new FileBlame(REPORT_SRC);
        report.setCommit(768, "11d9cdf38bd029d970705b1151aef910cd873044");
        report.setName(768, "Ulli Hafner");
        report.setEmail(768, "ullrich.hafner@gmail.com");
        report.setCommit(83, "2fcc7335c5b3570d5c624a94d43dc886e305b21a");
        report.setName(83, "Ulli Hafner");
        report.setEmail(83, "ullrich.hafner@gmail.com");
        report.setCommit(101, "cb183f0e9d97a49584f9fa4f932dceb9b24e9586");
        report.setName(101, "Ulli Hafner");
        report.setEmail(101, "ullrich.hafner@gmail.com");

        assertThat(blames.getBlame(REPORT)).isEqualTo(report);
    }

    private Path createTempFile() {
        try {
            return Files.createTempFile("test", ".blames");
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

}
