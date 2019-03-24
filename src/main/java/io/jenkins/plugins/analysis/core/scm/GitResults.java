package io.jenkins.plugins.analysis.core.scm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GitResults implements Serializable {
    private Map<String, GitResult> results = new HashMap<>();

    public Map<String, GitResult> getResults() {
        return results;
    }

    public void setResults(final Map<String, GitResult> results) {
        this.results = results;
    }

}
