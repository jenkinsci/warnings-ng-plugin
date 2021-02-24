package io.jenkins.plugins.analysis.warnings;

import java.net.URL;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.Select;

import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Page object to configure the issues column in a {@link ListView}.
 *
 * @author Andreas Riepl
 * @author Oliver Scholz
 */
public class IssuesColumnConfiguration extends PageObject {
    private String jobName;
    private ListView listView;

    /**
     * Creates a new issue column configuration page object.
     *
     * @param injector
     *         injector
     * @param url
     *         the URL of the view
     */
    public IssuesColumnConfiguration(final Injector injector, final URL url) {
        super(injector, url);
    }

    /**
     * Creates a new issue column configuration page object.
     *
     * @param context
     *         context
     * @param url
     *         the URL of the view
     */
    protected IssuesColumnConfiguration(final PageObject context, final URL url) {
        super(context, url);
    }

    /**
     * Creates a new issue column configuration page object.
     *
     * @param parent
     *         the build that contains the static analysis results
     * @param jobName
     *         the name of the jenkins job
     * @param listView
     *         the associated view
     */
    public IssuesColumnConfiguration(final Build parent, final String jobName, final ListView listView) {
        super(parent, parent.url(""));

        this.jobName = jobName;
        this.listView = listView;
    }

    /**
     * checks the option "select subset of tools" and fills in a tool name.
     *
     * @param toolId
     *         the tool to select
     */
    public void selectSubsetOfTools(final String toolId) {
        listView.check("Select subset of tools");
        listView.fillIn("_.id", toolId);
    }

    /**
     * selects a type from the "Type"-dropdown.
     *
     * @param statisticProperty
     *         Property object holding the display name
     */
    public void selectType(final StatisticProperties statisticProperty) {
        // scroll to bottom of page to ensure visibility of dropdown
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");

        Select typeSelect = new Select(driver.findElement(By.name("_.type")));
        typeSelect.selectByVisibleText(statisticProperty.getDisplayName());
    }
}
