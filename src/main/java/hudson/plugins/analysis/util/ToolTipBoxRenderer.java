package hudson.plugins.analysis.util;

import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * {@link StackedBarRenderer} that delegates tooltip and URL generation to
 * separate objects.
 *
 * @author Ulli Hafner
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings("Eq")
public class ToolTipBoxRenderer extends StackedBarRenderer implements CategoryToolTipGenerator {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 3270603409987078410L;
    /** The tooltip generator for the clickable map. */
    private final SerializableToolTipGenerator toolTipGenerator;

    /**
     * Creates a new instance of {@link ToolTipBoxRenderer}.
     *
     * @param toolTipGenerator
     *            the tooltip generator for the clickable map
     */
    public ToolTipBoxRenderer(final SerializableToolTipGenerator toolTipGenerator) {
        super();
        this.toolTipGenerator = toolTipGenerator;
        setToolTipGenerator(this);
    }

    /** {@inheritDoc} */
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
        return toolTipGenerator.generateToolTip(dataset, row, column);
    }
}
