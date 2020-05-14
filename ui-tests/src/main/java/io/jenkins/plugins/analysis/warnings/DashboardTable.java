package io.jenkins.plugins.analysis.warnings;


import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.PageObject;

public class DashboardTable extends PageObject {

    private final String portletId;

    public DashboardTable(final Build parent, final URL url, final String portletId) {
        super(parent, url);
        this.portletId = portletId;

    }

    public WebDriver open() {
        return this.visit(this.url);
    }

    public List<String> getPaneHeaders() {
        WebElement table = getTable();
        return table.findElements(by.xpath("./thead/tr/th"))
            .stream()
            .map(e -> {
                List<WebElement> img = e.findElements(by.tagName("img"));
                if (img.size() > 0) {
                    return img.get(0).getAttribute("title");
                }

                return e.getText();
            })
            .map(String::trim)
            .collect(Collectors.toList());
    }

    public List<List<String>> getJobRows() {
        WebElement table = getTable();
        return table.findElements(by.xpath("./tbody/tr"))
            .stream()
            .map(e -> e.findElements(by.tagName("td"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList()))
            .collect(Collectors.toList());
    }

    // this.getElement(by.id(portletId)).findElement(by.id(portletId)).findElements(by.xpath("./tbody/tr")).get(0).getText()

    private WebElement getTable() {
        return this.getElement(by.id(portletId)).findElement(by.id(portletId));
    }
}
