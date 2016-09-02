package hudson.plugins.warnings;

import java.util.Collections;
import java.util.List;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import com.cloudbees.hudson.plugins.folder.computed.FolderComputation;
import javax.annotation.Nonnull;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.google.common.collect.Lists;

import static org.junit.Assert.*;

import hudson.model.Action;

/**
 * Tests the class {@link WarningsPublisher}.
 *
 * @author Ulli Hafner
 */
public class WarningsPublisherTest {
    private static final String SUFFIX_NAME = " Warnings";
    private static final String SECOND = "PyLint";
    private static final String FIRST = "Maven";
    private static final String PATTERN = "Pattern";

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    /**
     * Verifies that the order of warnings is preserved.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-14615">Issue 14615</a>
     */
    @Test 
    public void testIssue14615Console() throws Exception {
        String flow = "node {\n"
                    + "  step([$class: 'WarningsPublisher', consoleParsers: [[parserName: '" + FIRST  + "']]])\n"
                    + "  step([$class: 'WarningsPublisher', consoleParsers: [[parserName: '" + SECOND + "']]])\n"
                    + "}\n";
	
        List<Action> ordered = getListOfActions(flow);
        List<String> expected = createExpectedResult();

        Collections.reverse(ordered);
        Collections.reverse(expected);

        checkOrder(expected, ordered);
    }

    /**
     * Verifies that the order of warnings is preserved.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-14615">Issue 14615</a>
     */
    @Test
    public void testIssue14615File() throws Exception {
        String flow = "node {\n"
                    + "  step([$class: 'WarningsPublisher', parserConfigurations: [[parserName: '" + FIRST  + "', pattern: '" + PATTERN + "']]])\n"
                    + "  step([$class: 'WarningsPublisher', parserConfigurations: [[parserName: '" + SECOND + "', pattern: '" + PATTERN + "']]])\n"
                    + "}\n";
	
        List<Action> ordered = getListOfActions(flow);
        List<String> expected = createExpectedResult();

        Collections.reverse(ordered);
        Collections.reverse(expected);

        checkOrder(expected, ordered);
    }
    
    private List<Action> getListOfActions(String flow) throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "p");
        job.setDefinition(new CpsFlowDefinition(flow));
        job.scheduleBuild2(0);
        jenkinsRule.waitUntilNoActivity();

        return Lists.newArrayList(job.getLastBuild().getAllActions());
    }

    private List<String> createExpectedResult() {
        List<String> expected = Lists.newArrayList();
        expected.add(FIRST + SUFFIX_NAME);
        expected.add(SECOND + SUFFIX_NAME);
        return expected;
    }

    private void checkOrder(final List<String> expected, final List<Action> ordered) {
        for (int position = 0; position < ordered.size(); position++) {
            System.out.println(ordered.get(position).getDisplayName());
        }
	
        assertEquals("Wrong number of actions.", 8, ordered.size());

        for (int position = 0; position < expected.size(); position++) {
            assertPosition(ordered, expected, position);
        }
    }

    private void assertPosition(final List<Action> ordered, final List<String> expected, final int position) {
        int orderedPosition = (position * 2) + 5;
        assertEquals("Wrong action at position " + position, expected.get(position), ordered.get(orderedPosition).getDisplayName());
    }
}

