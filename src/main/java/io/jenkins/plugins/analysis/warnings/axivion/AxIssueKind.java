package io.jenkins.plugins.analysis.warnings.axivion;

public enum AxIssueKind {
    AV("architecture violations"),
    CL("clones"),
    CY("cycles"),
    DE("dead entities"),
    MV("metric violations"),
    SV("style violations");

    private final String plural;

    AxIssueKind(final String plural) {
        this.plural = plural;
    }

    public String plural() {
        return plural;
    }
}

