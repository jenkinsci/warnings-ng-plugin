package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * {@link PageObject} representing the analysis summary on the build page of a job.
 *
 * @author Ullrich Hafner
 * @author Manuel Hampp
 * @author Michaela Reitschuster
 * @author Alexandra Wenzel
 */
public class AnalysisSummary extends PageObject {
    private static final Pattern NUMBER = Pattern.compile("\\d+");
    private static final String UNDEFINED = "-";

    private final WebElement summary;
    private WebElement title;
    private List<WebElement> results;
    private final String id;

    /**
     * Creates a new page object representing the analysis summary on the build page of a job.
     *
     * @param parent
     *         a finished build configured with a static analysis tool
     * @param id
     *         the type of the result page (e.g. simian, checkstyle, cpd, etc.)
     */
    public AnalysisSummary(final Build parent, final String id) {
        super(parent, parent.url(id.toLowerCase()));

        this.id = id;
        summary = getElement(By.id(id + "-summary"));
        if (summary != null) {
            title = find(By.id(id + "-title"));
            results = summary.findElements(by.xpath("./ul/li"));
        }
    }

    /**
     * Determines whether this summary page exists.
     *
     * @return {@code true} if the page exists, {@code false} otherwise
     */
    public boolean isDisplayed() {
        return summary != null && summary.isDisplayed();
    }

    /**
     * Return the title text of the summary.
     *
     * @return the title text
     */
    public String getTitleText() {
        return title.getText();
    }

    /**
     * Returns the number of new issues.
     *
     * @return the number of new issues
     */
    public int getNewSize() {
        return getSize("new");
    }

    /**
     * Returns the number of fixed issues.
     *
     * @return the number of fixed issues
     */
    public int getFixedSize() {
        return getSize("fixed");
    }

    /**
     * Returns the reference build that is used to compute the number of new issues. If there is no such reference
     * build, then 0 is returned.
     *
     * @return the reference build
     */
    public int getReferenceBuild() {
        return getSize("#");
    }

    /**
     * Returns whether the tool produced some errors or not. If there are errors, then the info messages view will
     * contain errors and info messages. Otherwise, only info messages are shown.
     *
     * @return the type of the icon that links to the info messages view
     */
    public InfoType getInfoType() {
        String iconName = find(by.xpath("//a[@href='" + id + "/info']/i")).getAttribute("class");

        return InfoType.valueOfClass(iconName);
    }

    /**
     * Returns the tools that are part of the aggregated results. If aggregation is disabled, then {@link #UNDEFINED} is
     * returned.
     */
    public String getAggregation() {
        for (WebElement result : results) {
            String message = result.getText();
            String aggregation = "Static analysis results from: ";
            if (message.startsWith(aggregation)) {
                return StringUtils.removeStart(message, aggregation);
            }
        }
        return UNDEFINED;
    }

    /**
     * Returns the texts of the detail elements of the summary.
     *
     * @return the details
     */
    public List<String> getDetails() {
        return results.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private int getSize(final String linkName) {
        Optional<WebElement> newLink = findClickableResultEntryByNamePart(linkName);

        return newLink.map(webElement -> extractNumber(webElement.getText())).orElse(0);
    }

    private int extractNumber(final String linkText) {
        Matcher matcher = NUMBER.matcher(linkText);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(0));
        }
        else if (linkText.startsWith("One")) {
            return 1;
        }
        else {
            return 0;
        }
    }

    /**
     * Clicks the title link that opens the details page with the analysis results.
     *
     * @return the details page with the analysis result
     */
    public AnalysisResult openOverallResult() {
        return openPage(getTitleResultLink(), AnalysisResult.class);
    }

    /**
     * Clicks the info link that opens the messages page showing all info and error messages.
     *
     * @return the messages page showing all info and error messages
     */
    public InfoView openInfoView() {
        return openPage(getTitleResultInfoLink(), InfoView.class);
    }

    /**
     * Clicks the new link that opens details page with the analysis results - filtered by new issues.
     *
     * @return the details page with the analysis result
     */
    public AnalysisResult openNewIssues() {
        return openLink("new", "No new link found");
    }

    /**
     * Clicks the fixed link that opens details page with the fixed issues.
     *
     * @return the details page with the analysis result
     */
    public AnalysisResult openFixedIssues() {
        return openLink("fixed", "No fixed link found");
    }

    /**
     * Clicks the reference build link that opens details page with the analysis results of the reference build.
     *
     * @return the details page with the analysis result of the reference build
     */
    public AnalysisResult openReferenceBuildResults() {
        return openLink("Reference", "No reference build link found");
    }

    private AnalysisResult openLink(final String linkText, final String errorMessage) {
        Optional<WebElement> link = findClickableResultEntryByNamePart(linkText);
        if (link.isPresent()) {
            return openPage(link.get(), AnalysisResult.class);
        }
        throw new NoSuchElementException(errorMessage);
    }

    private <T extends PageObject> T openPage(final WebElement link, final Class<T> type) {
        String href = link.getAttribute("href");
        T result = newInstance(type, injector, url(href), id);
        link.click();

        return result;
    }

    /**
     * Returns the quality gate result of this parser, if set.
     *
     * @return Success - if the quality gate thresholds have not been reached. Failed - otherwise.
     */
    public QualityGateResult getQualityGateResult() {
        for (WebElement result : results) {
            if (result.getText().contains("Quality gate")) {
                return QualityGateResult.valueOf(
                        result.findElement(by.tagName("img")).getAttribute("title").toUpperCase());
            }
        }
        return QualityGateResult.INACTIVE;
    }

    /**
     * Gets the Webelement of the reset button.
     *
     * @throws org.openqa.selenium.NoSuchElementException
     *         When there is no quality gate reset button.
     */
    public WebElement getQualityGateResetButton() throws org.openqa.selenium.NoSuchElementException {
        for (WebElement result : results) {
            if (result.getText().contains("Quality gate")) {
                return result.findElement(by.id(id + "-resetReference"));
            }
        }
        throw new org.openqa.selenium.NoSuchElementException("Quality gate reset button not found");
    }

    /**
     * Checks if the quality gate reset button is present.
     *
     * @return True, if quality gate reset button is present.
     */
    public boolean hasQualityGateResetButton() {
        try {
            if (getQualityGateResetButton() != null) {
                return true;
            }
        } catch (org.openqa.selenium.NoSuchElementException ignored) { }

        return false;
    }

    /**
     * Returns a clickable WebElement (a-tag), by a part of the elements text.
     *
     * @param namePart
     *         part of the visible text (should be unique within the item list)
     *
     * @return WebElement that belongs to the name part
     */
    private Optional<WebElement> findClickableResultEntryByNamePart(final String namePart) {
        for (WebElement el : results) {
            if (el.getText().contains(namePart)) {
                return Optional.of(el.findElement(by.tagName("a")));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the complete visible text by a part of the elements text.
     *
     * @param namePart
     *         part of the visible text (should be unique within the item list)
     *
     * @return String that belongs to the name part
     */
    public String findResultEntryTextByNamePart(final String namePart) {
        for (WebElement el : results) {
            if (el.getText().contains(namePart)) {
                return el.getText();
            }
        }
        return null;
    }

    private WebElement getSummary() {
        return summary;
    }

    private WebElement getTitleResultLink() {
        return summary.findElement(by.href(id));
    }

    private WebElement getTitleResultInfoLink() {
        return summary.findElement(by.href(id + "/info"));
    }

    private WebElement getTitle() {
        return title;
    }

    /**
     * Determines which icon is shown to represent the info messages view.
     */
    public enum InfoType {
        /** Info messages only. */
        INFO("fa-info-circle"),
        /** Info and error messages. */
        ERROR("fa-exclamation-triangle");

        private String iconName;

        InfoType(final String iconName) {
            this.iconName = iconName;
        }

        static InfoType valueOfClass(final String iconName) {
            if (iconName.contains(INFO.iconName)) {
                return INFO;
            }
            if (iconName.contains(ERROR.iconName)) {
                return ERROR;
            }
            throw new NoSuchElementException("No such info type with classes " + iconName);
        }
    }

    /**
     * Determines the quality gate result.
     */
    public enum QualityGateResult {
        SUCCESS, FAILED, UNSTABLE, INACTIVE
    }
}
