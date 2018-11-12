package io.jenkins.plugins.analysis.warnings;

import org.jenkinsci.Symbol;
import org.junit.Test;

import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link FindBugs}.
 *
 * @author Ullrich Hafner
 */
public class FindBugsITest extends IntegrationTestWithJenkinsPerTest {
    /** Verifies that a {@link Tool} defines a {@link Symbol}. */
    @Test
    public void shouldProvideSymbol() {
        FindBugs findBugs = new FindBugs();
        
        assertThat(findBugs.getSymbolName()).isEqualTo("findBugs");
    }
}