package io.jenkins.plugins.analysis.warnings;

import java.net.URL;

import org.openqa.selenium.By;

import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * {@link PageObject} representing the groovy parser configuration in the global settings.
 */
public class GroovyConfiguration extends PageObject {
    private final String pathPrefix;
    private final String xpathPattern;

    protected GroovyConfiguration(final PageObject context, final URL url, final String pathPrefix,
            final String xpathPattern) {
        super(context, url);
        this.pathPrefix = pathPrefix;
        this.xpathPattern = xpathPattern;
    }

    /**
     * Enters the specified value into the {@code name} field of the groovy script.
     *
     * @param name
     *         to be entered.
     */
    public void enterName(final String name) {
        enterIntoInput(name, "name");
    }

    /**
     * Enters the specified value into the {@code id} field of the groovy script.
     *
     * @param id
     *         to be entered.
     */
    public void enterId(final String id) {
        enterIntoInput(id, "id");
    }

    /**
     * Enters the specified value into the {@code regex} field of the groovy script.
     *
     * @param regex
     *         to be entered.
     */
    public void enterRegex(final String regex) {
        enterIntoInput(regex, "regexp");
    }

    /**
     * Enters the specified value into the {@code script} field of the groovy script.
     *
     * @param script
     *         to be entered. Should not contain '\t' characters.
     */
    public void enterScript(final String script) {
        enterIntoInput(script, "script");
    }

    /**
     * Enters the specified value into the {@code exampleMessage} field of the groovy script.
     *
     * @param exampleMessage
     *         to be entered. Should not contain '\t' characters.
     */
    public void enterExampleLogMessage(final String exampleMessage) {
        enterIntoInput(exampleMessage, "example");
    }

    private void enterIntoInput(final String text, final String path) {
        driver.findElement(By.xpath(String.format(xpathPattern, pathPrefix + path))).sendKeys(text);
    }
}
