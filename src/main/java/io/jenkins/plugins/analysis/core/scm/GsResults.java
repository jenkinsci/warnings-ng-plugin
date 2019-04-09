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
}
