package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebDriver;

/**
 * Utilities to remove the scroller on the job configuration page.
 *
 * @author Ullrich Hafner
 */
public final class ScrollerUtil {
    /**
     * Hides the scroller tab bar that seems to intercept mouse clicks if a select or repeatable element is clicked.
     *
     * @param driver
     *         web driver
     */
    public static void hideScrollerTabBar(final WebDriver driver) {
//        WebElement element = driver.findElement(By.xpath("//div[contains(@class, 'jenkins-config-widgets')]"));
//        if (driver instanceof JavascriptExecutor) {
//            ((JavascriptExecutor) driver).executeScript("arguments[0].style.visibility='hidden'", element);
//        }
    }

    private ScrollerUtil() {
        // prevents instantiation
    }
}
