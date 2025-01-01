package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.*;

/**
 * Tests the class {@link IconLabelProvider}.
 *
 * @author Kezhi Xiong
 * @author Ullrich Hafner
 */
class IconLabelProviderTest {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String SYMBOL = "test-symbol";
    private static final String PATH = "/plugin/warnings-ng/icons/";
    private static final String SVG = PATH + SYMBOL + ".svg";
    private static final String DEFAULT_SVG = PATH + ID + ".svg";

    @Test
    void shouldCreateSymbolProvider() {
        assertThat(new SymbolIconLabelProvider(ID, NAME, EMPTY_DESCRIPTION, SYMBOL))
                .hasName(NAME)
                .hasSmallIconUrl(SYMBOL)
                .hasLargeIconUrl(SYMBOL);
    }

    @Test
    void shouldCreateSvgProvider() {
        assertThat(new SvgIconLabelProvider(ID, NAME, EMPTY_DESCRIPTION, SYMBOL))
                .hasName(NAME)
                .hasSmallIconUrl(SVG)
                .hasLargeIconUrl(SVG);
        assertThat(new SvgIconLabelProvider(ID, NAME, EMPTY_DESCRIPTION))
                .hasName(NAME)
                .hasSmallIconUrl(DEFAULT_SVG)
                .hasLargeIconUrl(DEFAULT_SVG);
    }

    @Test
    void shouldCreateIconProvider() {
        assertThat(new IconLabelProvider(ID, NAME, EMPTY_DESCRIPTION, SYMBOL))
                .hasName(NAME)
                .hasSmallIconUrl(PATH + SYMBOL + "-24x24.png")
                .hasLargeIconUrl(PATH + SYMBOL + "-48x48.png");
        assertThat(new IconLabelProvider(ID, NAME, EMPTY_DESCRIPTION))
                .hasName(NAME)
                .hasSmallIconUrl(PATH + ID + "-24x24.png")
                .hasLargeIconUrl(PATH + ID + "-48x48.png");
    }
}
