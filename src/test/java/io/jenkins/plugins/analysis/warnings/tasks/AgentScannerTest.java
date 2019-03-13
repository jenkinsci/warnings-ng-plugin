package io.jenkins.plugins.analysis.warnings.tasks;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.SerializableTest;

import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.MatcherMode;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

/**
 * Tests the class {@link AgentScanner}.
 *
 * @author Ullrich Hafner
 */
class AgentScannerTest extends SerializableTest<AgentScanner> {
    @Override
    protected AgentScanner createSerializable() {
        return createScanner("**/*");
    }

    private AgentScanner createScanner(final String includePattern) {
        return new AgentScanner("high", "normal", "function",
                CaseMode.CASE_SENSITIVE, MatcherMode.STRING_MATCH, includePattern, "",
                "utf-8");
    }

    @Test
    @Issue("JENKINS-55350")
    void shouldReadDifferentUtf8Files() {
        readUtf8File("RAC.CharacterConsts-UTF8-BOM.pas");
        readUtf8File("RAC.CharacterConsts-UTF-8-NO-BOM.pas");
    }

    private void readUtf8File(final String fileName) {
        Path path = getResourceAsFile(fileName);
        Report report = createScanner(fileName).invoke(path.getParent().toFile(), null);

        assertThat(report).hasSize(2);
    }
}