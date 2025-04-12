package io.jenkins.plugins.analysis.warnings.axivion;

import edu.hm.hafner.analysis.Issue;

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

    private final String pluralName;
    private final AxIssueTransformation transformation;

    AxIssueKind(final String pluralName,
            final AxIssueTransformation transformation) {
        this.pluralName = pluralName;
        this.transformation = transformation;
    }

    public String plural() {
        return pluralName;
    }

    public Issue transform(final AxRawIssue rawIssue) {
        return transformation.transform(rawIssue);
    }
}
