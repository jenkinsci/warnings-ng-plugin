package io.jenkins.plugins.analysis.warnings.axivion;

/**
 * Supported issue kinds by Axivion Suite.
 */
enum AxIssueKind {
    AV("architecture violations", DefaultTransformations::createAVIssue),
    CL("clones", DefaultTransformations::createCLIssue),
    CY("cycles", DefaultTransformations::createCYIssue),
    DE("dead entities", DefaultTransformations::createDEIssue),
    MV("metric violations", DefaultTransformations::createMVIssue),
    SV("style violations", DefaultTransformations::createSVIssue);

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

