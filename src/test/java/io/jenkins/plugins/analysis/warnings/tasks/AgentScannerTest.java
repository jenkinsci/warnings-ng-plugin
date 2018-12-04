package io.jenkins.plugins.analysis.warnings.tasks;

import edu.hm.hafner.util.SerializableTest;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.CaseMode;
import io.jenkins.plugins.analysis.warnings.tasks.TaskScanner.MatcherMode;

/**
 * Tests the class {@link AgentScanner}.
 *
 * @author Ullrich Hafner
 */
class AgentScannerTest extends SerializableTest<AgentScanner> {
    @Override
    protected AgentScanner createSerializable() {
        return new AgentScanner("high", "normal", "low", 
                CaseMode.CASE_SENSITIVE, MatcherMode.STRING_MATCH, "", "",
                "UTF_8");
    }
}