package io.jenkins.plugins.analysis.warnings.groovy;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import io.jenkins.plugins.analysis.core.util.JenkinsFacade;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory.StaticAnalysisToolFactory;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link ParserConfiguration}.
 *
 * @author Ullrich Hafner
 */
public class ParserConfigurationITest extends IntegrationTestWithJenkinsPerSuite {
    /** Verifies that there is no parser defined and one factory. */
    @Test
    public void shouldHaveNoParsersAndOneProviderConfiguredWhenCreated() {
        ParserConfiguration configuration = getConfiguration();

        assertThat(configuration.getParsers()).isEmpty();

        StaticAnalysisToolFactory provider = getToolProvider();

        assertThat(provider.getTools()).isEmpty();
    }

    /** Verifies that there is one parser defined and one factory. */
    @Test
    public void shouldProvideOneParserForToolsRegistry() {
        ParserConfiguration configuration = getConfiguration();
        configuration.setParsers(Collections.singletonList(createParser()));

        assertThat(configuration.getParsers()).hasSize(1);

        StaticAnalysisToolFactory provider = getToolProvider();

        assertThat(provider.getTools()).hasSize(1);

        Tool tool = provider.getTools().get(0);
        assertThat(tool.getActualId()).isEqualTo("id");
        assertThat(tool.getActualName()).isEqualTo("name");
        
        assertThat(tool).isInstanceOf(ReportScanningTool.class);
        assertThat(((ReportScanningTool)tool).createParser()).isInstanceOf(DynamicLineParser.class);
    }

    private ParserConfiguration getConfiguration() {
        return ParserConfiguration.getInstance();
    }

    private StaticAnalysisToolFactory getToolProvider() {
        JenkinsFacade jenkinsFacade = new JenkinsFacade();
        List<StaticAnalysisToolFactory> providers = jenkinsFacade.getExtensionsFor(StaticAnalysisToolFactory.class);
        assertThat(providers).hasSize(1);
        return providers.get(0);
    }

    private GroovyParser createParser() {
        return new GroovyParser("id", "name", "regexp", "script", "example");
    }

}