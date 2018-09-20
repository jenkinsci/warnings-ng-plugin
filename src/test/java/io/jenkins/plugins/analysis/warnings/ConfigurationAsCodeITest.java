package io.jenkins.plugins.analysis.warnings;

import java.util.List;

import org.junit.Test;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.assertThat;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.groovy.GroovyParser;
import io.jenkins.plugins.analysis.warnings.groovy.ParserConfiguration;
import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.ConfiguratorException;

/**
 * Checks whether all parser can be imported using the configuration-as-code plug-in.
 *
 * @author Ullrich Hafner
 */
public class ConfigurationAsCodeITest extends IntegrationTestWithJenkinsPerTest {
    @Test
    public void shouldImportParserSettingsFromYaml() throws ConfiguratorException {
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

    private void configureJenkins(final String fileName) throws ConfiguratorException {
        ConfigurationAsCode.get().configure(ConfigurationAsCodeITest.class.getResource(fileName).toString());
    }
}
