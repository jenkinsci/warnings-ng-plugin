package hudson.plugins.warnings;

import javax.annotation.Nonnull;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.cloudbees.hudson.plugins.folder.computed.FolderComputation;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMHead;
import static org.junit.Assert.*;

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
     * Verifies that the graph is shown in multibranch job.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-31202">Issue 31202</a>
     */
    @Test @Ignore("Does not run on CI job")
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
        WebAssert.assertTextPresent(page, PARSER + SUFFIX_NAME + " Trend");
        WebAssert.assertTextPresent(page, "Enlarge");
        WebAssert.assertTextPresent(page, "Configure");
    }

    private static @Nonnull WorkflowJob scheduleAndFindBranchProject(@Nonnull WorkflowMultiBranchProject mp, @Nonnull String name) throws Exception {
        mp.scheduleBuild2(0).getFuture().get();
        return findBranchProject(mp, name);
    }

    private static @Nonnull WorkflowJob findBranchProject(@Nonnull WorkflowMultiBranchProject mp, @Nonnull String name) throws Exception {
        WorkflowJob p = mp.getItem(name);
        showIndexing(mp);
        if (p == null) {
            fail(name + " project not found");
        }
        return p;
    }

    private static void showIndexing(@Nonnull WorkflowMultiBranchProject mp) throws Exception {
        FolderComputation<?> indexing = mp.getIndexing();
        System.out.println("---%<--- " + indexing.getUrl());
        indexing.writeWholeLogTo(System.out);
        System.out.println("---%<--- ");
    }
}


