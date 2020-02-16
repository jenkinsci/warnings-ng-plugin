package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the class {@link IconLabelProvider}.
 *
 * @author Kezhi Xiong
 */
class IconLabelProviderTest {
    private static final String iconId = "icon-id";
    private static final String toolName = "tool-name";
    private static final String iconName = "icon-name";

    /**
     * This field has to be the same as in IconLabelProvider. Create this
     * to avoid using reflection or adding getters in the origin class.
     */
    private static final String ICONS_URL = "/plugin/warnings-ng/icons/";

    private static final String smallIconUrlSuffix = "-24x24.png";
    private static final String largeIconUrlSuffix = "-48x48.png";

    /**
     * Verifies that the name of the icon in URL is obtained from the icon id, if
     * the {@code iconName} parameter is empty.
     */
    @Test
    void shouldUseIdIfParameterIconNameIsBlank() {
        IconLabelProvider iconLabelProvider = new IconLabelProvider(iconId, toolName);

        assertThat(iconLabelProvider).hasId(iconId).hasName(toolName)
                .hasSmallIconUrl(ICONS_URL + iconId + smallIconUrlSuffix)
                .hasLargeIconUrl(ICONS_URL + iconId + largeIconUrlSuffix);
    }

    /**
     * Verifies the name of the icon in URL is obtained from parameter, if the
     * {@code iconName} parameter is provided.
     */
    @Test
    void shouldParameterNameIfNotBlank() {
        IconLabelProvider iconLabelProvider = new IconLabelProvider(iconId, toolName, iconName);

        assertThat(iconLabelProvider).hasId(iconId).hasName(toolName)
                .hasSmallIconUrl(ICONS_URL + iconName + smallIconUrlSuffix)
                .hasLargeIconUrl(ICONS_URL + iconName + largeIconUrlSuffix);
    }
}