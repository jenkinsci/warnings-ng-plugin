package io.jenkins.plugins.analysis.warnings;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openqa.selenium.WebElement;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.PageObject;

public class DashboardTable extends PageObject {
    private static final String EMPTY = "-";

    final List<String> headers;
    final Map<String, Map<String, DashboardTableEntry>> table;

    public DashboardTable(final Build parent, final URL url) {
        super(parent, url);
        this.open();

        WebElement page = this.getElement(by.tagName("body"));
        List<WebElement> rows = page.findElements(by.tagName("table")).stream()
            .filter(dom -> dom.getText().startsWith("Static analysis issues per tool and job"))
            .flatMap(dom -> dom.findElements(by.tagName("table")).stream().skip(1))
            .flatMap(dom -> dom.findElements(by.tagName("tr")).stream())
            .collect(Collectors.toList());

        headers = rows.stream()
            .flatMap(dom -> dom.findElements(by.tagName("th")).stream())
            .map(WebElement::getText)
            .collect(Collectors.toList());

        List<List<List<String>>> lines = rows.stream().skip(1)
                .map(dom -> dom.findElements(by.tagName("td")).stream().map(td -> {
                    if (td.findElements(by.tagName("a")).size() == 0) {
                        return Arrays.asList(td.getText(), null);
                    }
                    else {
                        WebElement entry = td.findElements(by.tagName("a")).get(0);
                        return Arrays.asList(entry.getText(), entry.getAttribute("href"));
                    }
                }).collect(Collectors.toList()))
                .collect(Collectors.toList());

        table = lines.stream()
                .collect(Collectors.toMap(entry -> entry.get(0).get(0), entry -> createPluginValueMapping(entry, headers)));
    }

    private Map<String, DashboardTableEntry> createPluginValueMapping(final List<List<String>> warnings, final List<String> plugins) {
        Map<String, DashboardTableEntry> valuePluginMapping = new HashMap<>();
        for (int i = 1; i < warnings.size(); i++) {
            if (!EMPTY.equals(warnings.get(i).get(0))) {
                valuePluginMapping.put(plugins.get(i).trim(), new DashboardTableEntry(Integer.parseInt(warnings.get(i).get(0)), warnings.get(i).get(1)));
            }
        }
        return valuePluginMapping;
    }

    /**
     * Represents entry in dashboard table with warnings count and link to the plugin page.
     */
    public static class DashboardTableEntry {
        private final int warningsCount;
        private final String url;

        /**
         * Construct a DashboardTableEntry.
         * @param warningsCount of the plugin in the build
         * @param url link to the plugin page
         */
        public DashboardTableEntry(final int warningsCount, final String url) {
            this.warningsCount = warningsCount;
            this.url = url;
        }

        public int getWarningsCount() {
            return warningsCount;
        }

        public String getUrl() {
            return url;
        }
    }
}
