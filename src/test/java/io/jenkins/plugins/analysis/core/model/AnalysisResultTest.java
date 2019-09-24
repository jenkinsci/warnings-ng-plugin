package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.ResourceTest;

import hudson.XmlFile;
import hudson.util.XStream2;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the class {@link AnalysisResult}.
 *
 * @author Ullrich Hafner
 */
class AnalysisResultTest extends ResourceTest {
    @Test
    void shouldRestoreResultBeforeIssuesStatisticsField() throws IOException {
        XStream2 reportXmlStream = new XStream2();

        Path xml = getResourceAsFile("result.xml");
        XmlFile xmlFile = new XmlFile(reportXmlStream, xml.toFile());
        AnalysisResult restored = (AnalysisResult) xmlFile.read();

        assertThat(restored).hasTotalSize(14).hasNewSize(9).hasFixedSize(0);
        assertThat(restored.getTotals()).hasTotalSize(14).hasNewSize(9).hasFixedSize(0);
    }
}
