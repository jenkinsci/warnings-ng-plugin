package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.model.Assertions.*;

/**
 * Tests the class {@link DefaultLabelProvider}.
 *
 * @author Ullrich Hafner
 */
class DefaultLabelProviderTest {
    private static final String ID = "id";
    private static final String NAME = "name";

    @Test
    void shouldReturnIdAndNameOfConstructorParametersInAllDisplayProperties() {
        DefaultLabelProvider labelProvider = new DefaultLabelProvider(ID, NAME);

        assertThat(labelProvider).hasId(ID);
        assertThat(labelProvider).hasName(NAME);
        assertThat(labelProvider.getLinkName()).contains(NAME);
        assertThat(labelProvider.getTrendName()).contains(NAME);
        assertThat(labelProvider.getResultUrl()).contains(ID);
    }

    @Test
    void shouldReturnIdAndDefaultNameIfNoNameIsGiven() {
        DefaultLabelProvider emptyNameLabelProvider = new DefaultLabelProvider(ID, "");

        assertThat(emptyNameLabelProvider).hasId(ID);
        assertThat(emptyNameLabelProvider).hasName(emptyNameLabelProvider.getDefaultName());

        DefaultLabelProvider nullNameLabelProvider = new DefaultLabelProvider(ID, null);

        assertThat(nullNameLabelProvider).hasId(ID);
        assertThat(nullNameLabelProvider).hasName(nullNameLabelProvider.getDefaultName());

        DefaultLabelProvider noNameLabelProvider = new DefaultLabelProvider(ID);

        assertThat(noNameLabelProvider).hasId(ID);
        assertThat(noNameLabelProvider).hasName(noNameLabelProvider.getDefaultName());
    }
}