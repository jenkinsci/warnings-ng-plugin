package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.By;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;

/**
 * Global system configuration of the Warnings Plugin.
 */
public class GlobalWarningsSettings extends JenkinsConfig {
    private static final String XPATH_PLUGIN_CONFIG = "//*[@path='%s']";

    private static final String GROOVY_PATH = "/io-jenkins-plugins-analysis-warnings-groovy-ParserConfiguration/";
    private static final String BUTTON_ADD = "repeatable-add";
    private static final String PARSERS_PREFIX = "parsers/";

    GlobalWarningsSettings(final Jenkins jenkins) {
        super(jenkins);
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
