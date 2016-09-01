package hudson.plugins.warnings;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;
import jenkins.scm.api.SCMHead;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.WebAssert;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import com.cloudbees.hudson.plugins.folder.computed.FolderComputation;
import javax.annotation.Nonnull;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import hudson.model.Action;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * Tests the class {@link WarningsPublisher}.
 *
 * @author Ulli Hafner
 */
public class WarningsMultiBranchTest {
    private static final String SUFFIX_NAME = " Warnings";
    private static final String PARSER = "Maven";
    private static final String PATTERN = "Pattern";
    
    @Rule public JenkinsRule jenkinsRule = new JenkinsRule();
    @Rule public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

   /**
     * Verifies that the order of warnings is preserved.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-31202">Issue 31202</a>
     */
    @Test 
    public void testIssue31202() throws Exception {
        sampleRepo.init();
	sampleRepo.write("Jenkinsfile", "" 
	               + "node {\n"
	               + "  checkout scm\n"
	               + "  step([$class: 'WarningsPublisher', parserConfigurations: [[parserName: '" + PARSER + "', pattern: '" + PATTERN + "']]])\n"
		       + "}\n"
	);
        sampleRepo.git("add", "Jenkinsfile"); 
	sampleRepo.git("commit", "--all", "--message=flow1");
	
	WorkflowMultiBranchProject mp = jenkinsRule.jenkins.createProject(WorkflowMultiBranchProject.class, "p");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false), new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        WorkflowJob p = scheduleAndFindBranchProject(mp, "master");
        assertEquals(new SCMHead("master"), SCMHead.HeadByItem.findHead(p));
        assertEquals(1, mp.getItems().size());
        jenkinsRule.waitUntilNoActivity();
        WorkflowRun b1 = p.getLastBuild();
        assertEquals(1, b1.getNumber());
	sampleRepo.write("new", "empty");
        sampleRepo.git("add", "new"); 
	sampleRepo.git("commit", "--all", "--message=flow2");
        p = scheduleAndFindBranchProject(mp, "master");
	jenkinsRule.waitUntilNoActivity();
        b1 = p.getLastBuild();
        assertEquals(2, b1.getNumber());
	
	HtmlPage page = jenkinsRule.createWebClient().getPage(p,"");
	WebAssert.assertTextPresent(page, "Pipeline master");
	WebAssert.assertTextPresent(page, PARSER);
	WebAssert.assertTextPresent(page, "Enlarge");
	WebAssert.assertTextPresent(page, "Configure");
    }

    public static @Nonnull WorkflowJob scheduleAndFindBranchProject(@Nonnull WorkflowMultiBranchProject mp, @Nonnull String name) throws Exception {
        mp.scheduleBuild2(0).getFuture().get();
        return findBranchProject(mp, name);
    }

    public static @Nonnull WorkflowJob findBranchProject(@Nonnull WorkflowMultiBranchProject mp, @Nonnull String name) throws Exception {
        WorkflowJob p = mp.getItem(name);
        showIndexing(mp);
        if (p == null) {
            fail(name + " project not found");
        }
        return p;
    }

    static void showIndexing(@Nonnull WorkflowMultiBranchProject mp) throws Exception {
        FolderComputation<?> indexing = mp.getIndexing();
        System.out.println("---%<--- " + indexing.getUrl());
        indexing.writeWholeLogTo(System.out);
        System.out.println("---%<--- ");
    }
}


