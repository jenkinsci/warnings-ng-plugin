package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

/**
 * {@link PageObject} for the SnippetGenerator to learning the available Pipeline steps.
 *
 * @author Lion Kosiuk
 */
public class SnippetGenerator extends PageObject {
    private static final String URI = "pipeline-syntax/";
    private static final String RECORD_ISSUES_OPTION = "recordIssues: Record compiler warnings and static analysis results";
    private final Control selectSampleStep = control("/");
    private final WorkflowJob job;

    /**
     * Creates a new page object.
     *
     * @param context
     *         job context
     */
    public SnippetGenerator(final WorkflowJob context) {
        super(context,  context.url(URI));

        job = context;
    }

    /**
     * Set the sample step of the SnippetGenerator to record Issues.
     *
     * @return issuesRecorder
     */
    public IssuesRecorder selectRecordIssues() {
        selectSampleStep.select(RECORD_ISSUES_OPTION);
        elasticSleep(2000);
        return new IssuesRecorder(job, "/prototype");
    }

    /**
     * Generates the sample pipeline script.
     *
     * @return script
     */
    public String generateScript() {
        WebElement button = find(By.id("generatePipelineScript-button"));
        button.click();
        elasticSleep(500);
        WebElement textarea = find(By.id("prototypeText"));
        return textarea.getAttribute("value");
    }
}
