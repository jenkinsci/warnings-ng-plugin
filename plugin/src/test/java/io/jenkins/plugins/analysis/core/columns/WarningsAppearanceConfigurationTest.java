package io.jenkins.plugins.analysis.core.columns;

import org.junit.jupiter.api.Test;

import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.util.IssuesStatistics.StatisticProperties;
import io.jenkins.plugins.util.GlobalConfigurationFacade;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

class WarningsAppearanceConfigurationTest {
    @Test
    void shouldInitializeConfiguration() {
        var facade = mock(GlobalConfigurationFacade.class);
        var jenkins = mock(JenkinsFacade.class);
        var configuration = new WarningsAppearanceConfiguration(facade, jenkins);

        assertThat(configuration.doFillDefaultTypeItems()).isEmpty();
        when(jenkins.hasPermission(Jenkins.READ)).thenReturn(true);
        assertThat(configuration.doFillDefaultTypeItems()).map(o -> o.value)
                .contains("TOTAL",
                        "TOTAL_ERROR",
                        "TOTAL_HIGH",
                        "TOTAL_NORMAL",
                        "TOTAL_LOW",
                        "TOTAL_MODIFIED",
                        "NEW",
                        "NEW_ERROR",
                        "NEW_HIGH",
                        "NEW_NORMAL",
                        "NEW_LOW",
                        "NEW_MODIFIED",
                        "DELTA",
                        "DELTA_ERROR",
                        "DELTA_HIGH",
                        "DELTA_NORMAL",
                        "DELTA_LOW",
                        "FIXED");

        verify(facade).load();

        assertThat(configuration).isEnableColumnByDefault();
        assertThat(configuration).hasDefaultType(StatisticProperties.TOTAL);
        assertThat(configuration).hasDefaultName(Messages.IssuesTotalColumn_Label());

        configuration.setEnableColumnByDefault(false);

        verify(facade).save();
        assertThat(configuration).isNotEnableColumnByDefault();

        configuration.setDefaultType(StatisticProperties.NEW);

        verify(facade, times(2)).save();
        assertThat(configuration).hasDefaultType(StatisticProperties.NEW);

        var name = "Warnings";
        configuration.setDefaultName(name);

        verify(facade, times(3)).save();
        assertThat(configuration).hasDefaultName(name);
    }
}
