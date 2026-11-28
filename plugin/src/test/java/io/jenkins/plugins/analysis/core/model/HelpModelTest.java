package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.Severity;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.HelpModel.HelpEntry;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests the class {@link HelpModel}.
 *
 * @author Ullrich Hafner
 */
class HelpModelTest {
    /**
     * Verifies the properties that are required to render the page.
     */
    @Test
    void shouldProvidePageProperties() {
        Run<?, ?> run = mock(Run.class);

        var model = new HelpModel(run);

        assertThat(model).hasDisplayName(Messages.HelpModel_DisplayName()).hasUrlName("help");
        assertThat(model.getObject()).isSameAs(run);
        assertThat(model.getTab().getObject()).isSameAs(run);
    }

    /**
     * Verifies that all baselines are described, in the same order as on the issues page.
     */
    @Test
    void shouldDescribeAllBaselines() {
        var model = new HelpModel(mock(Run.class));

        assertThat(model.getBaselines())
                .extracting(HelpEntry::getName)
                .containsExactly(
                        Messages.HelpModel_Baseline_all_Name(),
                        Messages.HelpModel_Baseline_new_Name(),
                        Messages.HelpModel_Baseline_outstanding_Name(),
                        Messages.HelpModel_Baseline_fixed_Name());
        assertThat(model.getBaselines())
                .extracting(HelpEntry::getDescription)
                .allSatisfy(text -> assertThat(text).isNotBlank());
    }

    /**
     * Verifies that all predefined severities are described, ordered from the highest to the lowest severity.
     */
    @Test
    void shouldDescribeAllSeverities() {
        var model = new HelpModel(mock(Run.class));

        assertThat(model.getSeverities())
                .extracting(HelpEntry::getName)
                .containsExactly(
                        LocalizedSeverity.getLongLocalizedString(Severity.ERROR),
                        LocalizedSeverity.getLongLocalizedString(Severity.WARNING_HIGH),
                        LocalizedSeverity.getLongLocalizedString(Severity.WARNING_NORMAL),
                        LocalizedSeverity.getLongLocalizedString(Severity.WARNING_LOW));
        assertThat(model.getSeverities())
                .extracting(HelpEntry::getDescription)
                .allSatisfy(text -> assertThat(text).isNotBlank());
    }

    /**
     * Verifies that each type of quality gate is described.
     *
     * @param type
     *         the type of the quality gate
     */
    @ParameterizedTest(name = "{0} should have a description")
    @EnumSource(QualityGateType.class)
    void shouldDescribeAllQualityGateTypes(final QualityGateType type) {
        var model = new HelpModel(mock(Run.class));

        assertThat(model.getQualityGateTypes())
                .hasSize(QualityGateType.values().length)
                .filteredOn(entry -> entry.getName().equals(type.getDisplayName()))
                .singleElement()
                .satisfies(entry -> assertThat(entry.getDescription()).isNotBlank());
    }

    /**
     * Verifies that each criticality of a quality gate is described with the icon of the resulting status.
     *
     * @param criticality
     *         the criticality of the quality gate
     */
    @ParameterizedTest(name = "{0} should have a description")
    @EnumSource(QualityGateCriticality.class)
    void shouldDescribeAllCriticalities(final QualityGateCriticality criticality) {
        var model = new HelpModel(mock(Run.class));

        assertThat(model.getCriticalities())
                .hasSize(QualityGateCriticality.values().length)
                .filteredOn(entry -> entry.getName().equals(criticality.name()))
                .singleElement()
                .satisfies(entry -> assertThat(entry)
                        .hasIcon(criticality.getStatus().getIconClass())
                        .hasDescription(OverviewModel.describeCriticality(criticality)));
    }
}
