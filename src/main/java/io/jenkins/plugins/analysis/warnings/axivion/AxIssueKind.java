package io.jenkins.plugins.analysis.warnings.axivion;

public enum AxIssueKind {
    AV("architecture violations", AxIssueTransformation::createAVIssue),
    CL("clones", AxIssueTransformation::createCLIssue),
    CY("cycles", AxIssueTransformation::createCYIssue),
    DE("dead entities", AxIssueTransformation::createDEIssue),
    MV("metric violations", AxIssueTransformation::createMVIssue),
    SV("style violations", AxIssueTransformation::createSVIssue);

    private final String plural;
    private final AxIssueTransformation transformation;

    AxIssueKind(final String plural,
            final AxIssueTransformation transformation) {
        this.plural = plural;
        this.transformation = transformation;
    }

    public String plural() {
        return plural;
    }

    public AxIssueTransformation getTransformation() {
        return transformation;
    }
}

