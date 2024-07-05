package io.jenkins.plugins.analysis.warnings;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * {@link PageObject} representing the dashboard on home.
 *
 * @author Lukas Kirner
 */
@SuppressFBWarnings("EI")
public class DashboardTable extends PageObject {
    private static final String EMPTY = "-";

    private final List<String> headers;
    private final Map<String, Map<String, DashboardTableEntry>> table;

    /**
     * Creates a new page object representing the dashboard.
     *
     * @param parent
     *         a finished build
     * @param url
     *         the type of the result page (e.g., simian, checkstyle, cpd, etc.)
     */
    @SuppressFBWarnings("MC")
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public DashboardTable(final Build parent, final URL url) {
        super(parent, url);

        open(); // TODO: the view should be already open when calling this constructor

        WebElement page = this.getElement(by.tagName("body"));
        List<WebElement> rows = page.findElements(by.tagName("table")).stream()
                .filter(dom -> dom.getText().startsWith("Static analysis issues per tool and job"))
                .flatMap(dom -> dom.findElements(by.tagName("table")).stream().skip(1))
                .flatMap(dom -> dom.findElements(by.tagName("tr")).stream())
                .collect(Collectors.toList());

        headers = rows.stream()
                .flatMap(dom -> dom.findElements(by.tagName("th")).stream())
                .map(th -> {
                    List<WebElement> images = th.findElements(by.tagName("img"));
                    if (images.isEmpty()) {
                        return th.getText();
                    }
                    else {
                        String src = images.get(0).getAttribute("src");
                        return src.substring(src.lastIndexOf('/'));
                    }
                })
                .collect(Collectors.toList());

        List<List<List<String>>> lines = rows.stream().skip(1)
                .map(dom -> dom.findElements(by.tagName("td")).stream().map(td -> {
                    if (td.findElements(by.tagName("a")).isEmpty()) {
                        return Arrays.asList(td.getText(), null);
                    }
                    else {
                        WebElement entry = td.findElements(by.tagName("a")).get(0);
                        return Arrays.asList(entry.getText(), entry.getAttribute("href"));
                    }
                }).collect(Collectors.toList()))
                .collect(Collectors.toList());

        table = lines.stream()
                .collect(Collectors.toMap(entry -> entry.get(0).get(0),
                        entry -> createPluginValueMapping(entry, headers)));
    }

    public List<String> getHeaders() {
        return this.headers;
    }

    public Map<String, Map<String, DashboardTableEntry>> getTable() {
        return this.table;
    }

    private Map<String, DashboardTableEntry> createPluginValueMapping(final List<List<String>> warnings,
            final List<String> plugins) {
        Map<String, DashboardTableEntry> valuePluginMapping = new HashMap<>();
        for (int i = 1; i < warnings.size(); i++) {
            if (!EMPTY.equals(warnings.get(i).get(0))) {
                valuePluginMapping.put(plugins.get(i).trim(),
                        new DashboardTableEntry(Integer.parseInt(warnings.get(i).get(0)), warnings.get(i).get(1)));
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
         *
         * @param warningsCount
         *         of the plugin in the build
         * @param url
         *         link to the plugin page
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
