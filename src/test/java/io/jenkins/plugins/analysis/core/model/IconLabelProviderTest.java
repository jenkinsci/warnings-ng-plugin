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
     * This field has to be the same as in IconLabelProvider. Create this
     * to avoid using reflection or adding getters in the origin class.
     */
    private static final String ICONS_URL = "/plugin/warnings-ng/icons/";

    private static final String SMALL_ICON_URL_SUFFIX = "-24x24.png";
    private static final String LARGE_ICON_URL_SUFFIX = "-48x48.png";

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
        IconLabelProvider iconLabelProvider = new IconLabelProvider(ICON_ID, TOOL_NAME, ICON_NAME);

        assertThat(iconLabelProvider).hasId(ICON_ID).hasName(TOOL_NAME)
                .hasSmallIconUrl("/plugin/warnings-ng/icons/icon-name-24x24.png")
                .hasLargeIconUrl("/plugin/warnings-ng/icons/icon-name-48x48.png");
    }
}