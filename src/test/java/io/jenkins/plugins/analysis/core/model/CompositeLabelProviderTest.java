package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.model.Assertions.*;

/**
 * Tests the class {@link CompositeLabelProvider}.
 *
 * @author Ullrich Hafner
 */
class CompositeLabelProviderTest {
    private static final String ID = "id";
    private static final String NAME = "name";

    @Test
    void shouldReturnIdAndNameOfConstructorParametersInAllDisplayProperties() {
        CompositeLabelProvider labelProvider = new CompositeLabelProvider(new StaticAnalysisLabelProvider(ID), NAME);

        assertThat(labelProvider).hasId(ID);
        assertThat(labelProvider).hasName(NAME);
        assertThat(labelProvider.getLinkName()).contains(NAME);
        assertThat(labelProvider.getTrendName()).contains(NAME);
        assertThat(labelProvider.getResultUrl()).contains(ID);
    }
}