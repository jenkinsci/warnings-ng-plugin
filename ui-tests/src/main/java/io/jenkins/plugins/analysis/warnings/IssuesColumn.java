package io.jenkins.plugins.analysis.warnings;

import java.net.URL;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * {@link PageObject} to access column "# Issues" in ListView or Dashboard.
 *
 * @author Andreas Riepl
 * @author Oliver Scholz
 */
public class IssuesColumn extends PageObject {
    private String jobName;

    /**
     * Creates a new issue column page object.
     *
     * @param injector
     *         injector
     * @param url
     *         the URL of the view
     */
    @SuppressWarnings("unused") // Required to dynamically create page object using reflection
    public IssuesColumn(final Injector injector, final URL url) {
        super(injector, url);
    }

    /**
     * Creates a new issue column page object.
     *
     * @param context
     *         context
     * @param url
     *         the URL of the view
     */
    @SuppressWarnings("unused") // Required to dynamically create page object using reflection
    protected IssuesColumn(final PageObject context, final URL url) {
        super(context, url);
    }

    /**
     * Creates a new issue column page object.
     *
     * @param parent
     *         the build that contains the static analysis results
     * @param jobName
     *         the name of the jenkins job
     */
    public IssuesColumn(final Build parent, final String jobName) {
        super(parent, parent.url(""));

        this.jobName = jobName;
    }

    /**
     * Retrieves the {@link WebElement} to access the issue count in the view column.
     *
     * @return Table Cell that contains the Issue Count as {@link WebElement}
     */
    public WebElement getIssuesCountFromTable() {
        return driver.findElement(by.xpath("//*[@id=\"job_" + jobName + "\"]/td[8]"));
    }

    /**
     * Search for a a-Tag in table cell.
     *
     * @return {@code true} if table cell contains link; otherwise {@code false}
     */
    public boolean issuesCountFromTableHasLink() {
        try {
            getIssuesCountFromTable().findElement(by.xpath("//*[@id=\"job_" + jobName + "\"]/td[8]/a"));
            return true;
        }
        catch (final NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Reads the count from the table as text.
     *
     * @return Displayed Issue Count as {@link String}
     */
    public String getIssuesCountTextFromTable() {
        return getIssuesCountFromTable().getText();
    }

    /**
     * Reads the tool name from the table that is displayed when hovering the issue column.
     *
     * @param rowNumber
     *         number of the row in the displayed table
     *
     * @return Name of the tool in the given row
     */
    public String getToolNameFromHover(final int rowNumber) {
        return findIfNotVisible(by.xpath(
                "//*[@id=\"job_" + jobName + "\"]/td[8]/div/table/tbody/tr[" + rowNumber + "]/td[2]")).getText();
    }

    /**
     * Reads the issue count from the table that is displayed when hovering the issue column.
     *
     * @param rowNumber
     *         number of the row in the displayed table
     *
     * @return issue count in the given row
     */
    public String getIssueCountFromHover(final int rowNumber) {
        return findIfNotVisible(by.xpath(
                "//*[@id=\"job_" + jobName + "\"]/td[8]/div/table/tbody/tr[" + rowNumber + "]/td[3]")).getText();
    }

    /**
     * Hovers over the issue count in list view column.
     */
    public void hoverIssueCount() {
        WebElement we = getIssuesCountFromTable();

        Actions action = new Actions(driver);
        action.moveToElement(we).perform();
    }
}
