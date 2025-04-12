package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import edu.hm.hafner.util.ResourceTest;

import java.util.Collections;

import hudson.model.Run;

import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.util.QualityGateResult;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link AnalysisResult}.
 *
 * @author Ullrich Hafner
 */
class AnalysisResultTest extends ResourceTest {
    @Test
    @Issue("SECURITY-2090")
    void constructorShouldThrowExceptionIfIdHasInvalidPattern() {
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> new AnalysisResult(mock(Run.class), "../../invalid-id", mock(DeltaReport.class),
                                new Blames(), new RepositoryStatistics(),
                                new QualityGateResult(), Collections.emptyMap()));
    }
}
