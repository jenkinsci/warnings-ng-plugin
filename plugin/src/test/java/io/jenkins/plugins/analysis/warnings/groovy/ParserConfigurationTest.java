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
        GroovyParser additionalParser = new GroovyParser("1", "", "", "", "");

        ParserConfiguration configuration = new ParserConfiguration(facade);
        configuration.addParser(additionalParser);

        verify(facade).save();
        assertThat(configuration.getParsers()).containsExactly(additionalParser);
    }

    @Test
    void shouldThrowIfParserExists() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);
        GroovyParser testParser = new GroovyParser("1", "", "", "", "");

        ParserConfiguration configuration = new ParserConfiguration(facade);
        configuration.addParser(testParser);
        verify(facade).save();

        assertThatIllegalArgumentException().isThrownBy(() -> configuration.addParser(testParser)).withMessageContaining(testParser.getId());
    }

    @Test
    void deleteShouldRemoveOnlySpecifiedParser() {
        GlobalConfigurationFacade facade = mock(GlobalConfigurationFacade.class);
        GroovyParser firstTestParser = new GroovyParser("1", "", "", "", "");
        GroovyParser secondTestParser = new GroovyParser("2", "", "", "", "");

        ParserConfiguration configuration = new ParserConfiguration(facade);
        configuration.addParser(firstTestParser);
        configuration.addParser(secondTestParser);

        assertThat(configuration.getParsers()).containsExactly(firstTestParser, secondTestParser);

        configuration.deleteParser(firstTestParser.getId());
        assertThat(configuration.getParsers()).containsExactly(secondTestParser);
    }
}
