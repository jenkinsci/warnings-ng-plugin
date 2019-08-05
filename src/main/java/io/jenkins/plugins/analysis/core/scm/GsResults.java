package io.jenkins.plugins.analysis.core.scm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GsResults implements Serializable {
    private static final long serialVersionUID = -3286184482030185494L;
    private Map<String, GsResult> results = new HashMap<>();

    public Map<String, GsResult> getResults() {
        return results;
    }

    public void setResults(final Map<String, GsResult> results) {
        this.results = results;
    }

    public void addAll(final GsResults other) {
        results.putAll(
                other.getResults()); // overwrites results for the same file, but GsResult will contain the same information anyway
    }

    public boolean contains(final String fileName) {
        return results.containsKey(fileName);
    }

    public GsResult get(final String fileName) {
        return results.get(fileName);
    }

    public void add(final String fileName, final GsResult gsResult) {
        results.put(fileName, gsResult);
    }
}
