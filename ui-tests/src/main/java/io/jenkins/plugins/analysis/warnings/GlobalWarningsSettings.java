package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.By;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;

public class GlobalWarningsSettings extends JenkinsConfig {

    private static final String XPATH_PLUGIN_CONFIG = "//*[@path='/io-jenkins-plugins-analysis-core-model-WarningsPluginConfiguration/%s']";

    public GlobalWarningsSettings(final Jenkins jenkins) {
        super(jenkins);
    }

    public void enterSourceDirectoryPath(final String absolutePath) {
        ensureConfigPage();

        driver.findElement(By.xpath(String.format(XPATH_PLUGIN_CONFIG, "repeatable-add"))).click();
        driver.findElement(By.xpath(String.format(XPATH_PLUGIN_CONFIG, "sourceDirectories/path")))
                .sendKeys(absolutePath);
    }

    public String getHomeDirectory() {
        return driver.findElement(By.xpath("//td[contains(text(), 'Home directory')]//..//*[@class='setting-main']"))
                .getText();
    }

}
