package io.jenkins.plugins.analysis.warnings.groovy;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import io.jenkins.plugins.analysis.core.model.LabelProviderFactory.StaticAnalysisToolFactory;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link ParserConfiguration}.
 *
 * @author Ullrich Hafner
 */
class ParserConfigurationITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String ID = "id";
    private static final String NAME = "name";

    /** Verifies that there is no parser defined and one factory. */
    @Test
    void shouldHaveNoParsersAndOneProviderConfiguredWhenCreated() {
        var configuration = getConfiguration();

        assertThat(configuration.getParsers()).isEmpty();

        var provider = getToolProvider();

        assertThat(provider.getLabelProvider(ID)).isEmpty();
    }

    /** Verifies that there is one parser defined and one factory. */
    @Test
    void shouldProvideOneParserForToolsRegistry() {
        var configuration = getConfiguration();
        configuration.setParsers(Collections.singletonList(createParser()));

        assertThat(configuration.getParsers()).hasSize(1);

        var provider = getToolProvider();

        assertThat(provider.getLabelProvider(ID)).isNotEmpty().hasValueSatisfying(
                labelProvider -> {
                    assertThat(labelProvider.getId()).isEqualTo(ID);
                    assertThat(labelProvider.getName()).isEqualTo(NAME);
                }
        );
    }

    private ParserConfiguration getConfiguration() {
        return ParserConfiguration.getInstance();
    }

    private StaticAnalysisToolFactory getToolProvider() {
        var jenkinsFacade = new JenkinsFacade();
        List<StaticAnalysisToolFactory> providers = jenkinsFacade.getExtensionsFor(StaticAnalysisToolFactory.class);
        assertThat(providers).hasSize(1);
        return providers.get(0);
    }

    private GroovyParser createParser() {
        return new GroovyParser(ID, NAME, "regexp", "script", "example");
    }
}
