package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the class {@link IconLabelProvider}.
 *
 * @author Kezhi Xiong
 */
class IconLabelProviderTest {
    private static final String ICON_ID = "icon-id";
    private static final String TOOL_NAME = "tool-name";
    private static final String ICON_NAME = "icon-name";

    /**
     * Verifies that the name of the icon in URL is obtained from the icon id, if
     * the {@code iconName} parameter is empty.
     */
    @Test
    void shouldUseIdIfParameterIconNameIsBlank() {
        IconLabelProvider iconLabelProvider = new IconLabelProvider(ICON_ID, TOOL_NAME);

        assertThat(iconLabelProvider).hasId(ICON_ID).hasName(TOOL_NAME)
                .hasSmallIconUrl("/plugin/warnings-ng/icons/icon-id-24x24.png")
                .hasLargeIconUrl("/plugin/warnings-ng/icons/icon-id-48x48.png");
    }

    /**
     * Verifies the name of the icon in URL is obtained from parameter, if the
     * {@code iconName} parameter is provided.
     */
    @Test
    void shouldParameterNameIfNotBlank() {
        IconLabelProvider iconLabelProvider = new IconLabelProvider(ICON_ID, TOOL_NAME,
                StaticAnalysisLabelProvider.EMPTY_DESCRIPTION, ICON_NAME
        );

        assertThat(iconLabelProvider).hasId(ICON_ID).hasName(TOOL_NAME)
                .hasSmallIconUrl("/plugin/warnings-ng/icons/icon-name-24x24.png")
                .hasLargeIconUrl("/plugin/warnings-ng/icons/icon-name-48x48.png");
    }
}
