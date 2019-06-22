package io.jenkins.plugins.analysis.warnings;


import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;
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
public class ConfigurationAsCodeITest extends IntegrationTestWithJenkinsPerTest {
    /**
     * Reads the YAML file with a parser and verifies that the parser has been loaded.
     */
    @Test
    public void shouldImportParserSettingsFromYaml() {
        configureJenkins("parsers.yaml");

        List<GroovyParser> parsers = ParserConfiguration.getInstance().getParsers();
        assertThat(parsers).hasSize(1);

        GroovyParser parser = parsers.get(0);

        assertThat(parser.getId()).isEqualTo("my-id");
        assertThat(parser.getName()).isEqualTo("my-name");
        assertThat(parser.getRegexp()).isEqualTo(".*");
        assertThat(parser.getExample()).isEqualTo("example");
        assertThat(parser.getScript()).isEqualTo("script");
    }

    /**
     * Reads the YAML file with a freestyle job and verifies that the job has been created.
     */
    @Test @Issue("JENKINS-57817") @Ignore("See https://issues.jenkins-ci.org/browse/JENKINS-57817")
    public void shouldFreestyleJobWithSpotBugsUsingJobDsl() {
        configureJenkins("job-dsl-spotbugs.yaml");

        TopLevelItem job = getJenkins().jenkins.getItem("freestyle-analysis-model");

        assertThat(job).isInstanceOf(FreeStyleProject.class);
        DescribableList<Publisher, Descriptor<Publisher>> publishers = ((FreeStyleProject) job).getPublishersList();
        assertThat(publishers).hasSize(1);
        assertThat(publishers.get(0)).isInstanceOf(IssuesRecorder.class);
    }

    /**
     * Reads the YAML file with a freestyle job and verifies that the job has been created.
     */
    @Test
    public void shouldFreestyleJobWithCheckstyleUsingJobDsl() {
        configureJenkins("job-dsl-checkstyle.yaml");

        TopLevelItem job = getJenkins().jenkins.getItem("freestyle-analysis-model");

        assertThat(job).isInstanceOf(FreeStyleProject.class);
        DescribableList<Publisher, Descriptor<Publisher>> publishers = ((FreeStyleProject) job).getPublishersList();
        assertThat(publishers).hasSize(1);
        Publisher publisher = publishers.get(0);
        assertThat(publisher).isInstanceOf(IssuesRecorder.class);
        IssuesRecorder recorder = (IssuesRecorder) publisher;
        List<Tool> tools = recorder.getTools();
        assertThat(tools).hasSize(1);
        assertThat(tools.get(0)).isInstanceOf(CheckStyle.class);
        assertThat(tools.get(0).getId()).isEqualTo("checkstyle-id");
        assertThat(tools.get(0).getName()).isEqualTo("checkstyle-name");

    }

    /**
     * Reads the YAML file with a freestyle job and verifies that the job has been created.
     */
    @Test
    public void shouldFreestyleJobWithTaskScannerUsingJobDsl() {
        configureJenkins("job-dsl-taskScanner.yaml");

        TopLevelItem job = getJenkins().jenkins.getItem("freestyle-analysis-model");

        assertThat(job).isInstanceOf(FreeStyleProject.class);
        DescribableList<Publisher, Descriptor<Publisher>> publishers = ((FreeStyleProject) job).getPublishersList();
        assertThat(publishers).hasSize(1);
        Publisher publisher = publishers.get(0);
        assertThat(publisher).isInstanceOf(IssuesRecorder.class);

        IssuesRecorder recorder = (IssuesRecorder) publisher;
        List<Tool> tools = recorder.getTools();
        assertThat(tools).hasSize(1);
        assertThat(tools.get(0)).isInstanceOf(OpenTasks.class);
        assertThat(tools.get(0).getId()).isEqualTo("taskScanner-id");
        assertThat(tools.get(0).getName()).isEqualTo("taskScanner-name");
    }

    /**
     * Reads the YAML file with a freestyle job and verifies that the job has been created.
     */
    @Test
    public void shouldFreestyleJobWithPmdParserUsingJobDsl() {
        configureJenkins("job-dsl-pmd.yaml");

        TopLevelItem job = getJenkins().jenkins.getItem("freestyle-analysis-model");

        assertThat(job).isInstanceOf(FreeStyleProject.class);
        DescribableList<Publisher, Descriptor<Publisher>> publishers = ((FreeStyleProject) job).getPublishersList();
        assertThat(publishers).hasSize(1);
        Publisher publisher = publishers.get(0);
        assertThat(publisher).isInstanceOf(IssuesRecorder.class);

        IssuesRecorder recorder = (IssuesRecorder) publisher;
        List<Tool> tools = recorder.getTools();
        assertThat(tools).hasSize(1);
        assertThat(tools.get(0)).isInstanceOf(Pmd.class);
        assertThat(tools.get(0).getId()).isEqualTo("pmd-id");
        assertThat(tools.get(0).getName()).isEqualTo("pmd-name");
    }

    /**
     * Reads the YAML file with a freestyle job and verifies that the job has been created.
     */
    @Test
    public void shouldFreestyleJobWithCpdUsingJobDsl() {
        configureJenkins("job-dsl-cpd.yaml");

        TopLevelItem job = getJenkins().jenkins.getItem("freestyle-analysis-model");

        copySingleFileToWorkspace(job, "cpd1Warning.xml", "target/cpd.xml");

        assertThat(job).isInstanceOf(FreeStyleProject.class);
        DescribableList<Publisher, Descriptor<Publisher>> publishers = ((FreeStyleProject) job).getPublishersList();
        assertThat(publishers).hasSize(1);
        Publisher publisher = publishers.get(0);
        assertThat(publisher).isInstanceOf(IssuesRecorder.class);

        AnalysisResult result = scheduleBuildAndAssertStatus((FreeStyleProject) job, Result.SUCCESS);

        HealthReport healthReport = ((FreeStyleProject) job).getBuildHealth();
        assertThat(healthReport.getScore()).isEqualTo(100);

        assertThat(result.getTotalSize()).isEqualTo(2);
        assertThat(result.getTotalErrorsSize()).isEqualTo(0);

        assertThat(result.getOutstandingIssues()).hasSize(2);

        IssuesRecorder recorder = (IssuesRecorder) publisher;
        List<Tool> tools = recorder.getTools();
        assertThat(tools).hasSize(1);
        assertThat(tools.get(0)).isInstanceOf(Cpd.class);
        assertThat(tools.get(0).getId()).isEqualTo("cpd-id");
        assertThat(tools.get(0).getName()).isEqualTo("cpd-name");
    }

    /**
     * Reads the YAML file with a freestyle job and verifies that the job has been created.
     */
    @Test
    public void shouldCreateFreestyleJobUsingJobDsl() {
        configureJenkins("job-dsl-freestyle.yaml");

        TopLevelItem job = getJenkins().jenkins.getItem("freestyle-analysis-model");

        assertThat(job).isInstanceOf(FreeStyleProject.class);
        DescribableList<Publisher, Descriptor<Publisher>> publishers = ((FreeStyleProject) job).getPublishersList();
        assertThat(publishers).hasSize(1);
        Publisher publisher = publishers.get(0);
        assertThat(publisher).isInstanceOf(IssuesRecorder.class);

        IssuesRecorder recorder = (IssuesRecorder) publisher;
        List<Tool> tools = recorder.getTools();
        assertThat(tools).hasSize(1);
        assertThat(tools.get(0)).isInstanceOf(Java.class);
        assertThat(tools.get(0).getId()).isEqualTo("java-id");
        assertThat(tools.get(0).getName()).isEqualTo("java-name");
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
