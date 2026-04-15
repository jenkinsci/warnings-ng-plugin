package io.jenkins.plugins.analysis.core.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hudson.FilePath;
import hudson.model.Result;
import io.jenkins.plugins.mcp.server.junit.JenkinsMcpClientBuilder;
import io.jenkins.plugins.mcp.server.junit.McpClientTest;
import io.modelcontextprotocol.spec.McpSchema;

import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class WarningsMcpServerExtensionTest {
    @McpClientTest
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    void testMcpToolCallGetWarnings(final JenkinsRule jenkins,
            final JenkinsMcpClientBuilder jenkinsMcpClientBuilder) throws Exception {
        WorkflowJob j = jenkins.createProject(WorkflowJob.class, "singleStep");
        j.setDefinition(new CpsFlowDefinition("""
                        stage('first') {
                          node {
                            recordIssues qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]], tools: [
                                checkStyle(pattern: '**/*.xml')
                            ]
                          }
                        }
                        """, true));
        FilePath ws = jenkins.jenkins.getWorkspaceFor(j);
        FilePath warningsFile = Objects.requireNonNull(ws).child("checkstyle.xml");
        URL checkstyleReport = WarningsMcpServerExtensionTest.class.getResource("checkstyle-sample.xml");
        assertThat(checkstyleReport).isNotNull();
        warningsFile.copyFrom(checkstyleReport);

        jenkins.buildAndAssertStatus(Result.UNSTABLE, j);

        try (var client = jenkinsMcpClientBuilder.jenkins(jenkins).build()) {
            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                    "getWarnings", Map.of("jobFullName", j.getFullName()));

            var response = client.callTool(request);
            assertContainsSingleCheckstyleWarning(response);
        }
        try (var client = jenkinsMcpClientBuilder.jenkins(jenkins).build()) {
            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                    "getWarnings", Map.of("jobFullName", j.getFullName(),
                    "checkId", "checkstyle"));

            var response = client.callTool(request);
            assertContainsSingleCheckstyleWarning(response);
        }
        try (var client = jenkinsMcpClientBuilder.jenkins(jenkins).build()) {
            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                    "getWarnings", Map.of("jobFullName", j.getFullName(),
                    "checkId", "missing"));

            var response = client.callTool(request);
            assertResponseIsEmpty(response);
        }
    }

    @McpClientTest
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    void testMcpToolNoWarnings(final JenkinsRule jenkins,
                                       final JenkinsMcpClientBuilder jenkinsMcpClientBuilder) throws Exception {
        WorkflowJob j = jenkins.createProject(WorkflowJob.class, "singleStep");
        j.setDefinition(new CpsFlowDefinition("""
                        stage('first') {
                          node {
                            echo "no warnings"
                          }
                        }
                        """, true));

        try (var client = jenkinsMcpClientBuilder.jenkins(jenkins).build()) {
            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                    "getWarnings", Map.of("jobFullName", j.getFullName()));

            var response = client.callTool(request);
            assertResponseIsEmpty(response);
        }
    }

    @McpClientTest
    void testMcpToolNoJob(final JenkinsRule jenkins,
                               final JenkinsMcpClientBuilder jenkinsMcpClientBuilder) {
        try (var client = jenkinsMcpClientBuilder.jenkins(jenkins).build()) {
            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                    "getWarnings", Map.of("jobFullName", "missing"));

            var response = client.callTool(request);
            assertResponseIsEmpty(response);
        }
    }

    private void assertResponseIsEmpty(final McpSchema.CallToolResult response) {
        assertThat(response.isError()).isFalse();
        assertThat(response.content()).hasSize(1);
        McpSchema.Content firstItem = response.content().get(0);
        assertThat(firstItem.type()).isEqualTo("text");
        assertThat(((McpSchema.TextContent) firstItem).text()).contains("no results");
    }

    private void assertContainsSingleCheckstyleWarning(final McpSchema.CallToolResult response) {
        assertThat(response.isError()).isFalse();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).type()).isEqualTo("text");
        String responseText = ((McpSchema.TextContent) response.content().get(0)).text();
        JsonObject parsed = JsonParser.parseString(responseText).getAsJsonObject();
        JsonObject result = parsed.get("result").getAsJsonObject();

        assertThat(result.keySet()).isEqualTo(Set.of("checkstyle"));
        JsonArray warnings = result.get("checkstyle").getAsJsonArray();
        assertThat(warnings.size()).isEqualTo(1);
        assertThat(warnings.get(0).getAsJsonObject().keySet()).isEqualTo(
                Set.of("category", "message", "type", "severity", "fileName", "line"));
    }
}
