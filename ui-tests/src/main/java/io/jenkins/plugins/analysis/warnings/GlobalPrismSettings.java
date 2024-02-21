package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.By;

import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.Jenkins;

/**
 * Global security configuration of the Prism Plugin.
 */
public class GlobalPrismSettings extends GlobalSecurityConfig {
    private static final String XPATH_PLUGIN_CONFIG = "//*[@path='%s']";

    private static final String SOURCE_DIR_PATH = "/io-jenkins-plugins-prism-PrismConfiguration/";
    private static final String BUTTON_ADD = "repeatable-add";
    private static final String SOURCE_PATH_FIELD = "sourceDirectories/path";

    GlobalPrismSettings(final Jenkins jenkins) {
        super(jenkins);
    }

    /**
     * Enters the given source directory path on the system configuration page from jenkins.
     *
     * @param absolutePath
     *         source directory path as an absolute path.
     */
    public void enterSourceDirectoryPath(final String absolutePath) {
        ensureConfigPage();

        driver.findElement(By.xpath(String.format(XPATH_PLUGIN_CONFIG, SOURCE_DIR_PATH + BUTTON_ADD))).click();
        driver.findElement(By.xpath(String.format(XPATH_PLUGIN_CONFIG, SOURCE_DIR_PATH + SOURCE_PATH_FIELD)))
                .sendKeys(absolutePath);
    }
}
