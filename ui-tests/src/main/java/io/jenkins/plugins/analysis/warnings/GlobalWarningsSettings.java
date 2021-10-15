package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.By;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;

/**
 * Global system configuration of the Warnings Plugin.
 */
public class GlobalWarningsSettings extends JenkinsConfig {
    private static final String XPATH_PLUGIN_CONFIG = "//*[@path='%s']";

    private static final String PATH_PREFIX = "/io-jenkins-plugins-analysis-";
    private static final String GROOVY_PATH = PATH_PREFIX + "warnings-groovy-ParserConfiguration/";
    private static final String SOURCE_DIR_PATH = PATH_PREFIX + "core-model-WarningsPluginConfiguration/";
    private static final String BUTTON_ADD = "repeatable-add";
    private static final String SOURCE_PATH_FIELD = "sourceDirectories/path";
    private static final String PARSERS_PREFIX = "parsers/";

    GlobalWarningsSettings(final Jenkins jenkins) {
        super(jenkins);
    }

    /**
     * Enters the given source directory path on the system configuration page from jenkins.
     *
     * @param absolutePath
     *         source directory path as absolute path.
     */
    public void enterSourceDirectoryPath(final String absolutePath) {
        ensureConfigPage();

        driver.findElement(By.xpath(String.format(XPATH_PLUGIN_CONFIG, SOURCE_DIR_PATH + BUTTON_ADD))).click();
        driver.findElement(By.xpath(String.format(XPATH_PLUGIN_CONFIG, SOURCE_DIR_PATH + SOURCE_PATH_FIELD)))
                .sendKeys(absolutePath);
    }

    /**
     * Returns the home directory of the current jenkins instance which is displayed on the system configuration page.
     *
     * @return home directory of the current jenkins instance.
     */
    public String getHomeDirectory() {
        ensureConfigPage();

        return driver.findElement(By.xpath("//td[contains(text(), 'Home directory')]//..//*[@class='setting-main']"))
                .getText();
    }

    /**
     * Opens the groovy parser configuration section on the system configuration page from jenkins.
     *
     * @return GroovyConfiguration Page Object to fill the parser configuration.
     */
    public GroovyConfiguration openGroovyConfiguration() {
        ensureConfigPage();

        driver.findElement(By.xpath(String.format(XPATH_PLUGIN_CONFIG, GROOVY_PATH + BUTTON_ADD))).click();
        return new GroovyConfiguration(this, url, GROOVY_PATH + PARSERS_PREFIX, XPATH_PLUGIN_CONFIG);
    }
}
