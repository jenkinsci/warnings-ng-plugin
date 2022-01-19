package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

import edu.hm.hafner.util.ResourceTest;

import hudson.XmlFile;
import hudson.model.Run;
import hudson.util.XStream2;

import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    @Issue("SECURITY-2090")
    void constructorShouldThrowExceptionIfIdHasInvalidPattern() {
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> new AnalysisResult(mock(Run.class), "../../invalid-id", mock(DeltaReport.class),
                                new Blames(), new RepositoryStatistics(),
                                QualityGateStatus.PASSED, Collections.emptyMap()));
    }
}
