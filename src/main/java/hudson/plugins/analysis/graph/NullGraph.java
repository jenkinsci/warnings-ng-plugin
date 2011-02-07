package hudson.plugins.analysis.graph;

/**
 * Empty graph. Null object: this graph does not render anything. Additionally,
 * there is no enable graph link presented in the UI.
 *
 * @author Ulli Hafner
 */
public class NullGraph extends EmptyGraph {
    /** {@inheritDoc} */
    @Override
    public String getId() {
        return "NULL";
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDeactivated() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSelectable() {
        return false;
    }
}

