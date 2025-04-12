package io.jenkins.plugins.analysis.warnings.integrations;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import java.util.List;

import hudson.model.FreeStyleProject;

import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.SpotBugs;
import io.jenkins.plugins.analysis.warnings.groovy.GroovyParser;
import io.jenkins.plugins.analysis.warnings.groovy.ParserConfiguration;
import io.jenkins.plugins.analysis.warnings.tasks.OpenTasks;
import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.ConfiguratorException;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Checks whether all parser can be imported using the configuration-as-code plug-in.
 *
 * @author Ullrich Hafner
 */
class ConfigurationAsCodeITest extends IntegrationTestWithJenkinsPerTest {
    /**
     * Reads the YAML file with a parser and verifies that the parser has been loaded.
     */
    @Test
    void shouldImportParserSettingsFromYaml() {
        configureJenkins("parsers.yaml");

        List<GroovyParser> parsers = ParserConfiguration.getInstance().getParsers();
        assertThat(parsers).hasSize(1);

        var parser = parsers.get(0);

        assertThat(parser.getId()).isEqualTo("my-id");
        assertThat(parser.getName()).isEqualTo("my-name");
        assertThat(parser.getRegexp()).isEqualTo(".*");
        assertThat(parser.getExample()).isEqualTo("example");
        assertThat(parser.getScript()).isEqualTo("script");
    }

    /**
     * Reads the YAML file with a freestyle job and verifies that the job has been created.
     */
    @Test @Issue("JENKINS-57817")
    void shouldFreestyleJobWithSpotBugsUsingJobDsl() {
        configureJenkins("job-dsl-spotbugs.yaml");

        var recorder = getIssuesRecorder();
        List<Tool> tools = recorder.getTools();
        assertThat(tools).hasSize(1);

        var spotBugs = tools.get(0);
        assertThat(spotBugs).isInstanceOf(SpotBugs.class);
        assertThat(spotBugs.getId()).isEqualTo("bugs");
        assertThat(spotBugs.getName()).isEqualTo("SpotBugs Warnungen");
    }

    /**
     * Reads the YAML file with a freestyle job and verifies that the job has been created.
     */
    @Test
    void shouldFreestyleJobWithTaskScannerUsingJobDsl() {
        configureJenkins("job-dsl-taskScanner.yaml");

        var recorder = getIssuesRecorder();
        List<Tool> tools = recorder.getTools();
        assertThat(tools).hasSize(1);

        var actual = tools.get(0);
        assertThat(actual).isInstanceOfSatisfying(OpenTasks.class, t -> {
            assertThat(t.getId()).isEqualTo("taskScanner-id");
            assertThat(t.getName()).isEqualTo("taskScanner-name");
            assertThat(t.getHighTags()).isEqualTo("FIXME");
            assertThat(t.getNormalTags()).isEqualTo("TODO");
            assertThat(t.getIncludePattern()).isEqualTo("**/*.java");
            assertThat(t.getExcludePattern()).isEqualTo("target/**/*");
        });
    }

    /**
     * Reads the YAML file with a freestyle job and verifies that the job has been created.
     */
    @Test
    void shouldCreateFreestyleJobUsingJobDsl() {
        configureJenkins("job-dsl-freestyle.yaml");

        var recorder = getIssuesRecorder();
        List<Tool> tools = recorder.getTools();
        assertThat(tools).hasSize(1);

        var java = tools.get(0);
        assertThat(java).isInstanceOf(Java.class);
        assertThat(java.getId()).isEqualTo("java-id");
        assertThat(java.getName()).isEqualTo("java-name");
    }

    private IssuesRecorder getIssuesRecorder() {
        return getRecorder(getJob());
    }

    private FreeStyleProject getJob() {
        var job = getJenkins().jenkins.getItem("freestyle-analysis-model");

        assertThat(job).isInstanceOf(FreeStyleProject.class);

        return (FreeStyleProject) job;
    }

    private void configureJenkins(final String fileName) {
        try {
            ConfigurationAsCode.get().configure(getResourceAsFile(fileName).toUri().toString());
        }
        catch (ConfiguratorException e) {
            throw new AssertionError(e);
        }
    }
}
