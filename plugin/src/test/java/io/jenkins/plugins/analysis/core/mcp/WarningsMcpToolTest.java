package io.jenkins.plugins.analysis.core.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.mcp.server.junit.JenkinsMcpClientBuilder;
import io.jenkins.plugins.mcp.server.junit.McpClientTest;
import io.modelcontextprotocol.spec.McpSchema;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class WarningsMcpToolTest {
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
        URL checkstyleReport = WarningsMcpToolTest.class.getResource("checkstyle-sample.xml");
        assertThat(checkstyleReport).isNotNull();
        warningsFile.copyFrom(checkstyleReport);

        Run<?, ?> run = jenkins.buildAndAssertStatus(Result.UNSTABLE, j);
        assertThat(run).isNotNull();
        assertThat(run.getAction(ResultAction.class)).isNotNull();

        AnalysisResult warningsResult = run.getAction(ResultAction.class).getResult();

        try (var client = jenkinsMcpClientBuilder.jenkins(jenkins).build()) {
            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                    "getWarnings", Map.of("jobFullName", j.getFullName()));

            var response = client.callTool(request);

            // Assert response
            assertThat(response.isError()).isFalse();
            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).type()).isEqualTo("text");
            assertThat(response.content())
                    .first()
                    .isInstanceOfSatisfying(McpSchema.TextContent.class, textContent ->
                            assertThat(textContent.type()).isEqualTo("text")
                    );

            DocumentContext documentContext = JsonPath.using(Configuration.defaultConfiguration())
                    .parse(((McpSchema.TextContent) response.content().get(0)).text());

            var result = documentContext.read("$.result", Map.class);

            assertThat(result.keySet()).isEqualTo(Set.of("checkstyle"));
            Object warningsAction = result.get("checkstyle");
            assertThat(warningsAction).isInstanceOf(List.class);
            assertThat(((List<?>) warningsAction).size())
                    .isEqualTo(warningsResult.getIssues().size());
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
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    void testMcpToolNoJob(final JenkinsRule jenkins,
                               final JenkinsMcpClientBuilder jenkinsMcpClientBuilder) throws Exception {
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
}
