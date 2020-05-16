package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

public class SnippetGenerator extends PageObject {

    private static final String URI = "pipeline-syntax/";
    private static final String RECORD_ISSUES_OPTION = "recordIssues: Record compiler warnings and static analysis results";

    private final String id;

    public SnippetGenerator(WorkflowJob context) {
        super(context,  context.url(URI));

        this.id = URI;
    }

    public SnippetGenerator selectRecordIssues() {
        WebElement selectInput = find(By.xpath("//select[@path='/']/option[text() = '" + RECORD_ISSUES_OPTION + "']"));
        selectInput.click();
        return this;
    }

    public SnippetGenerator setTool(final String toolName) {
        WebElement selectInput = find(By.xpath("//select[@path='/prototype/toolProxies/']/option[text() = '" + toolName + "']"));
        selectInput.click();
        return this;
    }

    public String generateScript() {
        WebElement button = find(By.xpath("//button[contains(text(),'Generate Pipeline Script')]"));
        button.click();

        WebElement textarea = find(By.xpath("//textarea[@name='_.']"));

        return textarea.getAttribute("value");
    }
}
