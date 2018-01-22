package io.jenkins.plugins.analysis.warnings.groovy;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.model.ToolRegistry.StaticAnalysisToolFactory;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link ParserConfiguration}.
 *
 * @author Ullrich Hafner
 */
public class ParserConfigurationITest extends IntegrationTest {
    @Test
    public void shouldHaveNoParsersAndOneProviderConfiguredWhenCreated() {
        ParserConfiguration configuration = getConfiguration();

        assertThat(configuration.getParsers()).isEmpty();

        StaticAnalysisToolFactory provider = getToolProvider();

        assertThat(provider.getTools()).isEmpty();
    }

    @Test
    public void shouldProvideOneParserForToolsRegistry() {
        ParserConfiguration configuration = getConfiguration();
        configuration.setParsers(Collections.singletonList(createParser()));

        assertThat(configuration.getParsers()).hasSize(1);

        StaticAnalysisToolFactory provider = getToolProvider();

        assertThat(provider.getTools()).hasSize(1);

        StaticAnalysisTool tool = provider.getTools().get(0);
        assertThat(tool.getId()).isEqualTo("id");
        assertThat(tool.getName()).isEqualTo("name");
        assertThat(tool.createParser()).isInstanceOf(DynamicLineParser.class);
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