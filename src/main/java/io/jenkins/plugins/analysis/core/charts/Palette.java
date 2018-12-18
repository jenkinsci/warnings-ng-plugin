package io.jenkins.plugins.analysis.core.charts;

/**
 * Color palette for the static analysis tools.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("ALL")
public enum Palette {
    RED("#EF9A9A", "#FFCDD2"),
    PURPLE("#CE93D8", "#E1BEE7"),
    INDIGO("#9FA8DA", "#C5CAE9"),
    TEAL("#80CBC4", "#B2DFDB"),
    GREEN("#A5D6A7", "#C8E6C9"),
    LIME("#E6EE9C", "#F0F4C3"),
    YELLOW("#FFF59D", "#FFF9C4"),
    ORANGE("#FFCC80", "#FFE0B2"),
    BROWN("#BCAAA4", "#D7CCC8"),
    BLUE("#90CAF9", "#90CAF9");

    private final String normal;
    private final String hover;

    Palette(final String normal, final String hover) {
        this.normal = normal;
        this.hover = hover;
    }

    public String getNormal() {
        return normal;
    }

    public String getHover() {
        return hover;
    }
}
