package hudson.plugins.warnings.util;

import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

/**
 * {@link StackedBarRenderer} that delegates tooltip and URL generation to
 * separate objects.
 *
 * @author Ulli Hafner
 */
public class BoxRenderer extends StackedBarRenderer implements CategoryToolTipGenerator, CategoryURLGenerator {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 1827457945114238470L;
    /** The URL generator for the clickable map. */
    private final SerializableUrlGenerator urlGenerator;
    /** The tooltip generator for the clickable map. */
    private final SerializableToolTipGenerator toolTipGenerator;

    /**
     * Creates a new instance of {@link BoxRenderer}.
     *
     * @param urlGenerator
     *            the URL generator for the clickable map
     * @param toolTipGenerator
     *            the tooltip generator for the clickable map
     */
    public BoxRenderer(final SerializableUrlGenerator urlGenerator, final SerializableToolTipGenerator toolTipGenerator) {
        super();
        this.urlGenerator = urlGenerator;
        this.toolTipGenerator = toolTipGenerator;
        setItemURLGenerator(this);
        setToolTipGenerator(this);
    }

    /** {@inheritDoc} */
    public final String generateURL(final CategoryDataset dataset, final int row, final int column) {
        return urlGenerator.generateURL(dataset, row, column);
    }

    /** {@inheritDoc} */
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
        return toolTipGenerator.generateToolTip(dataset, row, column);
    }
}
