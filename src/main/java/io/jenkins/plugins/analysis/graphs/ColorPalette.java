package io.jenkins.plugins.analysis.graphs;

import java.awt.*;

import com.google.common.collect.ImmutableList;

/**
 * Replacement for Jenkins {@link hudson.util.ColorPalette} in order to get good looking graphs even if the green balls
 * plug-in is installed.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("javadoc")
public final class ColorPalette {
    /** Replaces Jenkins red. */
    public static final Color RED = new Color(0xEF, 0x29, 0x29); // NOCHECKSTYLE
    /** Replaces Jenkins yellow. */
    public static final Color YELLOW = new Color(0xFC, 0xE9, 0x4F); // NOCHECKSTYLE
    /** Replaces Jenkins blue. */
    public static final Color BLUE = new Color(0x72, 0x9F, 0xCF); // NOCHECKSTYLE
    /** Replaces Jenkins grey. */
    public static final Color GREY = new Color(0xAB, 0xAB, 0xAB); // NOCHECKSTYLE

    /**
     * Color list usable for generating line charts.
     */
    public static final ImmutableList<Color> LINE_GRAPH = ImmutableList.of(
            new Color(0xCC0000), new Color(0x3465a4), new Color(0x73d216), new Color(0xedd400));

    /**
     * Creates a new instance of {@link hudson.plugins.analysis.graph.ColorPalette}.
     */
    private ColorPalette() {
        // prevents instantiation
    }
}
