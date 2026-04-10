package io.jenkins.plugins.analysis.core.mcp;

import edu.hm.hafner.analysis.Issue;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.mcp.server.McpServerExtension;
import io.jenkins.plugins.mcp.server.annotation.Tool;
import io.jenkins.plugins.mcp.server.annotation.ToolParam;
import io.jenkins.plugins.mcp.server.extensions.util.JenkinsUtil;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jenkinsci.plugins.variant.OptionalExtension;

/**
 * MCP tool for warnings report.
 */
@OptionalExtension(requirePlugins = "mcp-server")
public class WarningsMcpTool implements McpServerExtension {
    /**
     * Retrieves the warnings from static analysis tools.
     *
     * @param buildNumber build number
     * @param jobFullName full job name
     * @param checkId check action ID
     * @return map in the form {checkId1:[{category:"CAT", message:"MSG", type:"TYPE", ...}]}
     */
    @Tool(
            description = "Retrieves the warnings from static analysis tools associated with a Jenkins build",
            annotations = @Tool.Annotations(destructiveHint = false))
    public Map<String, Object> getWarnings(
            @ToolParam(description = "Job full name of the Jenkins job (e.g., 'folder/job-name')")
            final String jobFullName,
            @Nullable
                    @ToolParam(
                            description =
                                    "Build number (optional, if not provided, returns the test results for last build)",
                            required = false)
                    final Integer buildNumber,
            @Nullable
                    @ToolParam(
                            description =
                                    "ID of the check action (optional, if not provided, all warnings are returned)",
                            required = false)
                    final String checkId) {
        Optional<Run> run = JenkinsUtil.getBuildByNumberOrLast(jobFullName, buildNumber);
        if (run.isPresent()) {
            List<ResultAction> warningActions = run.get().getActions(ResultAction.class);
            Map<String, Object> response = new HashMap<>();
            for (ResultAction warningAction : warningActions) {
                if (checkId != null && !warningAction.getId().equals(checkId)) {
                    continue;
                }
                var result = warningAction.getResult();
                if (result != null) {
                    response.put(
                            warningAction.getId(),
                            result.getIssues().stream().map(IssueJson::new).collect(Collectors.toList()));
                }
            }
            return response;
        }
        return Map.of();
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
