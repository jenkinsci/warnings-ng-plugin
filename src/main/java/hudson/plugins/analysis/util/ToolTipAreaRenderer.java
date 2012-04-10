package hudson.plugins.analysis.util;

import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;

import hudson.util.StackedAreaRenderer2;

/**
 * {@link StackedAreaRenderer} that delegates tooltip generation to
 * a separate object.
 *
 * @author Ulli Hafner
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings("Eq")
public class ToolTipAreaRenderer extends StackedAreaRenderer2 {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -7373322043128362094L;
    /** The tooltip generator for the clickable map. */
    private final SerializableToolTipGenerator toolTipGenerator;

    /**
     * Creates a new instance of {@link ToolTipAreaRenderer}.
     *
     * @param toolTipGenerator
     *            the tooltip generator for the clickable map
     */
    public ToolTipAreaRenderer(final SerializableToolTipGenerator toolTipGenerator) {
        super();
        this.toolTipGenerator = toolTipGenerator;
    }

    @Override
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
        return toolTipGenerator.generateToolTip(dataset, row, column);
    }
}
