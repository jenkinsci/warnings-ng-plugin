package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the class {@link FindBugs}.
 *
 * @author Ullrich Hafner
 */
public class FindBugsITest extends IntegrationTestWithJenkinsPerTest {
    @Test
    public void shouldProvideSymbol() {
        FindBugs findBugs = new FindBugs();
        
        assertThat(findBugs.getSymbolName()).isEqualTo("findBugs");
    }
}