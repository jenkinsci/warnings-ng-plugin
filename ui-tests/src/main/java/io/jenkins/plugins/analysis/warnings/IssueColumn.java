package io.jenkins.plugins.analysis.warnings;

import java.net.URL;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.PageObject;

public class IssueColumn extends PageObject {

    private String jobName;

    public IssueColumn(final Injector injector, final URL url) {
        super(injector, url);
    }

    protected IssueColumn(final PageObject context, final URL url) {
        super(context, url);
    }

    public IssueColumn(final Build parent, final String jobName) {
        super(parent, parent.url(""));

        this.jobName = jobName;
    }

    public WebElement getIssuesCountFromTable() {
        return driver.findElement(by.xpath("//*[@id=\"job_" + jobName + "\"]/td[8]"));
    }

    public String getIssuesCountTextFromTable() {
        return getIssuesCountFromTable().getText();
    }

    public String getToolNameFromHover(final int rowNumber) {
        return findIfNotVisible(by.xpath("//*[@id=\"job_" + jobName + "\"]/td[8]/div/table/tbody/tr[" + rowNumber + "]/td[2]")).getText();
    }

    public String getIssueCountFromHover(final int rowNumber) {
        return findIfNotVisible(by.xpath("//*[@id=\"job_" + jobName + "\"]/td[8]/div/table/tbody/tr[" + rowNumber + "]/td[3]")).getText();
    }

    public void hoverIssueCount() {
        WebElement we = getIssuesCountFromTable();

        Actions action = new Actions(driver);
        action.moveToElement(we).perform();
    }
}
