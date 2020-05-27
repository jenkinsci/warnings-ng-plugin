package io.jenkins.plugins.analysis.warnings;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.WebElement;

import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.PageObject;

public class TrendChart extends PageObject {

    protected TrendChart(final PageObject context, final URL url) {
        super(context, url);
    }

    private final WebElement page = this.getElement(by.tagName("page-body"));

    List<WebElement> rows = page.findElements(by.tagName("page-body")).stream()
            .filter(dom -> dom.getText().startsWith("Reference Comparison"))
            .flatMap(dom -> dom.findElements(by.tagName("table")).stream().skip(1))
            .flatMap(dom -> dom.findElements(by.tagName("tr")).stream())
            .collect(Collectors.toList());

}
