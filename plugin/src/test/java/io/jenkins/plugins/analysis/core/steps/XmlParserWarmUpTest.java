package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import edu.hm.hafner.analysis.Report;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests that verify the XML parser infrastructure warm-up logic introduced to fix JENKINS-66268.
 *
 * @author Akash Manna
 * @see <a href="https://issues.jenkins.io/browse/JENKINS-66268">JENKINS-66268</a>
 */
class XmlParserWarmUpTest {
    /**
     * Verifies that calling {@code warmUpXmlParsers} pre-loads the XML parser infrastructure
     * without throwing exceptions or logging errors to the report.
     */
    @Test
    @Issue("JENKINS-66268")
    void shouldNotLogErrorDuringWarmUp() {
        Report report = new Report();

        assertThatCode(() -> IssuesScanner.ReportPostProcessor.warmUpXmlParsers(report))
                .doesNotThrowAnyException();

        assertThat(report.getErrorMessages()).isEmpty();
    }
}
