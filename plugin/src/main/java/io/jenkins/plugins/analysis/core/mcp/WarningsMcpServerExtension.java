package io.jenkins.plugins.analysis.core.mcp;

import edu.hm.hafner.analysis.Issue;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.mcp.server.McpServerExtension;
import io.jenkins.plugins.mcp.server.annotation.Tool;
import io.jenkins.plugins.mcp.server.annotation.ToolParam;
import io.jenkins.plugins.mcp.server.extensions.util.JenkinsUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jenkinsci.plugins.variant.OptionalExtension;

/**
 * An extension of the for MCP server exposing
 * {@link io.jenkins.plugins.analysis.core.model.AnalysisResult}
 * associated with a given build as a JSON object.
 */
@OptionalExtension(requirePlugins = "mcp-server")
public class WarningsMcpServerExtension implements McpServerExtension {
    /**
     * Retrieves the warnings from static analysis tools.
     *
     * @param buildNumber build number
     * @param jobFullName full job name
     * @param checkId check action ID
     * @return map in the form {checkId1:[{category:"CAT", message:"MSG", type:"TYPE", ...}]}
     */
    @SuppressWarnings("rawtypes")
    @Tool(
            description = "Retrieves the warnings from static analysis tools associated with a Jenkins build",
            annotations = @Tool.Annotations(destructiveHint = false))
    public Map<String, Object> getWarnings(
            @ToolParam(description = "Job full name of the Jenkins job (e.g., 'folder/job-name')")
            final String jobFullName,
                    @ToolParam(
                            description =
                                    "Build number (optional, if not provided, returns the test results for last build)",
                            required = false)
                    final Integer buildNumber,
                    @ToolParam(
                            description =
                                    "ID of the check action (optional, if not provided, all warnings are returned)",
                            required = false)
                    final String checkId) {
        Optional<Run> run = JenkinsUtil.getBuildByNumberOrLast(jobFullName, buildNumber);
        if (run.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            run.get().getActions(ResultAction.class)
                    .stream()
                    .filter(action -> checkId == null || action.getId().equals(checkId))
                    .forEach(warningAction -> addToResponse(response, warningAction));
            return response;
        }
        return Map.of();
    }

    private void addToResponse(final Map<String, Object> response,
            final ResultAction warningAction) {
        var result = warningAction.getResult();
        response.put(
                warningAction.getId(),
                result.getIssues().stream().map(IssueJson::new).collect(Collectors.toList()));
    }

    private record IssueJson(
            String category,
            String message,
            String type,
            String severity,
            String fileName,
            int line) {
        IssueJson(final Issue issue) {
            this(
                    issue.getCategory(),
                    issue.getMessage(),
                    issue.getType(),
                    issue.getSeverity().toString(),
                    issue.getFileName(),
                    issue.getLineStart());
        }
    }
}
