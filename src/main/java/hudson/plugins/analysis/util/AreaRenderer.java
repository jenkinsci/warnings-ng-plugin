package hudson.plugins.analysis.util;

import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * {@link StackedAreaRenderer} that delegates tooltip and URL generation to
 * separate objects.
 *
 * @author Ulli Hafner
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings("Eq")
public class AreaRenderer extends ToolTipAreaRenderer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -6802385549191651555L;
    /** The URL generator for the clickable map. */
    private final SerializableUrlGenerator urlGenerator;

    /**
     * Creates a new instance of {@link AreaRenderer}.
     *
     * @param urlGenerator
     *            the URL generator for the clickable map
     * @param toolTipGenerator
     *            the tooltip generator for the clickable map
     */
    public AreaRenderer(final SerializableUrlGenerator urlGenerator, final SerializableToolTipGenerator toolTipGenerator) {
        super(toolTipGenerator);
        this.urlGenerator = urlGenerator;
    }

    @Override
    public final String generateURL(final CategoryDataset dataset, final int row, final int column) {
        return urlGenerator.generateURL(dataset, row, column);
    }
}
