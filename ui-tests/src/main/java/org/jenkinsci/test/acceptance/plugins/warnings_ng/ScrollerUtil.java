package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
        WebElement element = driver.findElement(By.xpath("//div[contains(@class, 'jenkins-config-widgets')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.visibility='hidden'", element);
    }

    private ScrollerUtil() {
        // prevents instantiation
    }
}
