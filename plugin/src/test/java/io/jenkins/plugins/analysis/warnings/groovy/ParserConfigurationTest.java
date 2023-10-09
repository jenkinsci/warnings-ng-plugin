package io.jenkins.plugins.analysis.warnings.groovy;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import hudson.util.FormValidation;
import io.jenkins.plugins.util.GlobalConfigurationFacade;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ParserConfiguration}.
 *
 * @author Ullrich Hafner
 */
class ParserConfigurationTest {
    private static final List<GroovyParser> PARSERS = Collections.singletonList(mock(GroovyParser.class));

    @Test
    void shouldHaveNoParsersWhenCreated() {
        ParserConfiguration configuration = new ParserConfiguration(mock(GlobalConfigurationFacade.class));

        assertThat(configuration.getParsers()).isEmpty();
    }

    @Test
    void shouldSaveConfigurationIfParsersAreSet() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);

        ParserConfiguration configuration = new ParserConfiguration(facade);
        configuration.setParsers(PARSERS);

        verify(facade).save();
        assertThat(configuration.getParsers()).isEqualTo(PARSERS);
    }

    @Test
    void shouldHaveConsoleLogScanningPermittedSetToFalseWhenCreated() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);

        ParserConfiguration configuration = new ParserConfiguration(facade);

        assertThat(configuration.isConsoleLogScanningPermitted()).isEqualTo(false);
    }

    @Test
    void shouldSaveConfigurationIfConsoleLogScanningPermittedIsSet() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);

        ParserConfiguration configuration = new ParserConfiguration(facade);
        configuration.setConsoleLogScanningPermitted(true);

        verify(facade).save();
        assertThat(configuration.isConsoleLogScanningPermitted()).isEqualTo(true);
    }

    @Test
    void shouldWarnUserIfConsoleLogScanningPermittedIsSet() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);

        ParserConfiguration configuration = new ParserConfiguration(facade);

        final FormValidation actualFalse = configuration.doCheckConsoleLogScanningPermitted(false);
        assertThat(actualFalse).isOk();
        final FormValidation actualTrue = configuration.doCheckConsoleLogScanningPermitted(true);
        assertThat(actualTrue.kind).isEqualTo(FormValidation.Kind.WARNING);
    }
    @Test
    void shouldSaveConfigurationIfParserIsAdded() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);
        GroovyParser test_parser = new GroovyParser("1", "", "", "", "");

        ParserConfiguration configuration = new ParserConfiguration(facade);
        configuration.addParser(test_parser);

        verify(facade).save();
        assertThat(configuration.getParsers().contains(test_parser));
    }
    @Test
    void shouldThrowIfParserExists() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);
        GroovyParser test_parser = new GroovyParser("1", "", "", "", "");

        ParserConfiguration configuration = new ParserConfiguration(facade);
        configuration.addParser(test_parser);
        verify(facade).save();

        assertThatIllegalArgumentException().isThrownBy(() -> configuration.addParser(test_parser));
    }
    @Test
    void deleteShouldRemoveOnlySpecifiedParser() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);
        GroovyParser first_test_parser = new GroovyParser("1", "", "", "", "");
        GroovyParser second_test_parser = new GroovyParser("2", "", "", "", "");

        ParserConfiguration configuration = new ParserConfiguration(facade);
        configuration.addParser(first_test_parser);
        configuration.addParser(second_test_parser);

        assertThat(configuration.getParsers().contains(first_test_parser));
        assertThat(configuration.getParsers().contains(second_test_parser));

        configuration.deleteParser(first_test_parser.getId());
        assertThat(!(configuration.getParsers().contains(first_test_parser)));
        assertThat(configuration.getParsers().contains(first_test_parser));
    }
}
