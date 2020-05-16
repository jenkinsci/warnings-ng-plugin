package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

public class SnippetGeneratorUiTest extends AbstractJUnitTest {

    @Test
    public void defaultConfigurationTest() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.save();

        SnippetGenerator snippetGenerator = new SnippetGenerator(job);
        snippetGenerator.open();
        snippetGenerator.selectRecordIssues().setTool("Java");

        String script = snippetGenerator.generateScript();

        assertThat(script).isEqualTo("recordIssues(tools: [java()])");
    }
}
