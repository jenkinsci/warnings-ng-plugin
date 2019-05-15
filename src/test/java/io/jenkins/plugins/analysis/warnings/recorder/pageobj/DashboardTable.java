package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.Project;

public class DashboardTable {

    private final List<List<String>> listEntries;
    private final Map<String, Integer> jobIndex = new HashMap<>();
    private final Map<String, Integer> pluginIndex = new HashMap<>();


    public DashboardTable(final HtmlPage page) {
        List<String> tableLines = page.getElementsByTagName("table").stream()
                .filter(domElement -> domElement.asText().startsWith("Static analysis issues per tool and job"))
                .flatMap(domElement -> domElement.getElementsByTagName("table").stream())
                .map(domElement -> Arrays.asList(domElement.asText().split("\n")))
                .findFirst().orElseThrow(() -> new RuntimeException("The Static analysis issues per tool and job couldn't be load"));

        listEntries = tableLines.stream().map(line -> Arrays.asList(line.split("\t"))).collect(Collectors.toList());

        for (int index = 1; index < listEntries.size(); index++) {
            jobIndex.put(listEntries.get(index).get(0), index);
        }

        for (int index = 1; index < listEntries.get(0).size(); index++) {
            pluginIndex.put(listEntries.get(0).get(index), index);
        }
    }

    public Optional<Integer> getWarningCount(final Project project, final String plugin) {
        return getWarningCount(project.getName(), plugin);
    }

    public Optional<Integer> getWarningCount(final String job, final String plugin) {
        Optional<Integer> result = Optional.empty();
        if (getJobIndex().containsKey(job) && getPluginIndex().containsKey(plugin)) {
            String count = getListEntries().get(getJobIndex().get(job)).get(getPluginIndex().get(plugin));
            if (!"-".equals(count)) {
                result = Optional.of(Integer.parseInt(count));
            }
        }
        return result;
    }

    public Map<String, Integer> getWarningCounts(final Project project) {
        return getWarningCounts(project.getName());
    }

    public Map<String, Integer> getWarningCounts(final String job) {
        Map<String, Integer> result = new HashMap<>();

        for (Map.Entry<String, Integer> entry : getPluginIndex().entrySet()) {
            getWarningCount(job, entry.getKey()).ifPresent(c -> result.put(entry.getKey(), c));
        }
        return result;
    }



    private List<List<String>> getListEntries() {
        return listEntries;
    }

    private Map<String, Integer> getJobIndex() {
        return jobIndex;
    }

    private Map<String, Integer> getPluginIndex() {
        return pluginIndex;
    }
}
