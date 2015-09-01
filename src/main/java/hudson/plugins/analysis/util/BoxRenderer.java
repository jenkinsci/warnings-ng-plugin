package hudson.plugins.analysis.util;

import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

/**
 * {@link StackedBarRenderer} that delegates tooltip and URL generation to
 * separate objects.
 *
 * @author Ulli Hafner
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("Eq")
public class BoxRenderer extends ToolTipBoxRenderer implements CategoryURLGenerator {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 1827457945114238470L;
    /** The URL generator for the clickable map. */
    private final SerializableUrlGenerator urlGenerator;

    /**
     * Creates a new instance of {@link BoxRenderer}.
     *
     * @param urlGenerator
     *            the URL generator for the clickable map
     * @param toolTipGenerator
     *            the tooltip generator for the clickable map
     */
    public BoxRenderer(final SerializableUrlGenerator urlGenerator, final SerializableToolTipGenerator toolTipGenerator) {
        super(toolTipGenerator);
        this.urlGenerator = urlGenerator;
        setItemURLGenerator(this);
    }

    @Override
    public final String generateURL(final CategoryDataset dataset, final int row, final int column) {
        return urlGenerator.generateURL(dataset, row, column);
    }
}
