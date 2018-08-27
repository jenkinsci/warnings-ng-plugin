package io.jenkins.plugins.analysis.core.charts;

/**
 * Color palette for the static analysis tools.
 *
 * @author Ullrich Hafner
 */
enum Palette {
    RED("#E53935", "#EF5350"),
    YELLOW_DARK("#FDD835", "#FFEB3B"),
    YELLOW("#FFEE58", "#FFF176"),
    YELLOW_LIGHT("#FFF59D", "#FFF9C4"),
    BLUE("#039BE5", "#29B6F6");
    
    private final String normal;
    private final String hover;

    Palette(final String hover, final String normal) {
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
