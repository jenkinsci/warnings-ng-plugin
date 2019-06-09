package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.Project;

/**
 * Page Object for a table that shows the warning summary of selected builds.
 *
 * @author Michael Schmid, Raphael Furch
 */
public class DashboardTable {

    private final Map<String, Map<String, DashboardTableEntry>> table;

    /**
     * Creates a new instance of {@link DashboardTable}.
     *
     * @param page
     *         the whole details HTML page
     */
    public DashboardTable(final HtmlPage page) {

        List<HtmlElement> rowElements = page.getElementsByTagName("table").stream()
                .filter(dom -> dom.asText().startsWith("Static analysis issues per tool and job"))
                .flatMap(dom -> dom.getElementsByTagName("table").stream().skip(1))
                .flatMap(dom -> dom.getElementsByTagName("tr").stream())
                .collect(Collectors.toList());

        List<String> header = rowElements.stream().flatMap(dom -> dom.getElementsByTagName("th").stream())
                .map(HtmlElement::asText)
                .collect(Collectors.toList());

        List<List<List<String>>> lines = rowElements.stream().skip(1)
                .map(dom -> dom.getElementsByTagName("td").stream().map(td -> {
                    if (td.getElementsByTagName("a").size() == 0) {
                        return Arrays.asList(td.asText(), null);
                    }
                    else {
                        HtmlElement entry = td.getElementsByTagName("a").get(0);
                        return Arrays.asList(entry.asText(), entry.getAttribute("href"));
                    }
                }).collect(Collectors.toList()))
                .collect(Collectors.toList());

        table = lines.stream()
                .collect(Collectors.toMap(entry -> entry.get(0).get(0), entry -> createPluginValueMapping(entry, header)));
    }

    /**
     * Creates mapping between plugins and warnings. If no warning exists for a plugin, it will not be included in the Map.
     *
     * @param warnings
     *         list of warnings and links to plugin pages
     * @param plugins
     *         list of plugins
     *
     * @return mapping between plugins and warnings counts.
     */
    private Map<String, DashboardTableEntry> createPluginValueMapping(final List<List<String>> warnings,
            final List<String> plugins) {
        Map<String, DashboardTableEntry> valuePluginMapping = new HashMap<>();
        for (int i = 1; i < warnings.size(); i++) {
            if (!"-".equals(warnings.get(i).get(0))) {
                valuePluginMapping.put(plugins.get(i), new DashboardTableEntry(Integer.parseInt(warnings.get(i).get(0)), warnings.get(i).get(1)));
            }
        }
        return valuePluginMapping;
    }

    /**
     * Gets Warnings counts for a specified job.
     *
     * @param job
     *         job name
     *
     * @return warnings count
     */
    public Map<String, Integer> getWarningCounts(final String job) {
        return getDashboardTableEntry(job).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getWarningsCount()));
    }


    /**
     * Gets Warnings counts for a specified job.
     *
     * @param project
     *         jenkins project
     *
     * @return warnings count
     */
    public Map<String, Integer> getWarningCounts(final Project project) {
        return getWarningCounts(project.getName());
    }

    /**
     * Get all information of a specified job from the dashboard.
     *
     * @param job
     *         job name
     *
     * @return warnings count and link to plugin page
     */
    public Map<String, DashboardTableEntry> getDashboardTableEntry(final String job) {
        return getListEntries().get(job);
    }

    /**
     * Get all information of a specified job from the dashboard.
     *
     * @param project
     *         jenkins project
     *
     * @return warnings count and link to plugin page
     */
    public Map<String, DashboardTableEntry> getDashboardTableEntry(final Project project) {
        return getDashboardTableEntry(project.getName());
    }

    /**
     * Gets the result if a specified job is on the dashboard.
     *
     * @param job
     *         job name
     *
     * @return result if the job is on the dashboard
     */
    public boolean containsJob(final String job) {
        return getListEntries().containsKey(job);
    }

    /**
     * Gets the result if a specified job is on the dashboard.
     *
     * @param project
     *         jenkins project
     *
     * @return result if the job is on the dashboard
     */
    public boolean containsJob(final Project project) {
        return containsJob(project.getName());
    }

    private Map<String, Map<String, DashboardTableEntry>> getListEntries() {
        return table;
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
