package io.jenkins.plugins.analysis.warnings.groovy;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

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
    void shouldSaveConfigurationIfParsersAreAdded() {
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
}
