package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page Object for a table that shows the warning summary of selected builds.
 *
 * @author Michael Schmid, Raphael Furch
 */
public class DashboardTable {

    private final Map<String, Map<String, Integer>> table;

    /**
     * Creates a new instance of {@link DashboardTable}.
     *
     * @param page
     *         the whole details HTML page
     */
    public DashboardTable(final HtmlPage page) {

        List<String> tableLines = page.getElementsByTagName("table")
                .stream()
                .filter(domElement -> domElement.asText().startsWith("Static analysis issues per tool and job"))
                .flatMap(domElement -> domElement.getElementsByTagName("table").stream())
                .map(domElement -> Arrays.asList(domElement.asText().split("\n")))
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException("The Static analysis issues per tool and job couldn't be load"));

        List<String> plugins = tableLines.stream().limit(1).flatMap(line -> Arrays.stream(line.split("\t")))
                .collect(Collectors.toList());

        table = tableLines.stream().skip(1).map(line -> Arrays.asList(line.split("\t")))
                .collect(Collectors.toMap(list -> list.get(0), list -> createPluginValueMapping(list, plugins)));

    }

    /**
     * Creates mapping between plugins and warnings counts. If no warning exists for a plugin, it will not be included
     * in the Map.
     *
     * @param warnings
     *         list of warnings
     * @param plugins
     *         list of plugins
     *
     * @return mapping between plugins and warnings counts.
     */
    private Map<String, Integer> createPluginValueMapping(final List<String> warnings, final List<String> plugins) {
        Map<String, Integer> valuePluginMapping = new HashMap<>();
        for (int i = 1; i < warnings.size(); i++) {
            if (!"-".equals(warnings.get(i))) {
                valuePluginMapping.put(plugins.get(i), Integer.parseInt(warnings.get(i)));
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
        return getListEntries().get(job);
    }

    private Map<String, Map<String, Integer>> getListEntries() {
        return table;
    }

}
