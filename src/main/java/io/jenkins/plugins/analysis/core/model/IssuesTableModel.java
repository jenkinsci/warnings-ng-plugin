package io.jenkins.plugins.analysis.core.model;

import java.util.stream.Collectors;

import edu.hm.hafner.analysis.Issues;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
public class IssuesTableModel {
    public String toJsonArray(final Issues<BuildIssue> issues) {
        StringBuilder builder = new StringBuilder();

        builder.append("{data = [");
        builder.append(issues.stream().map(issue -> issue.toJson()).collect(Collectors.joining(", ")));
        builder.append("]}");
        return builder.toString();
    }

}
