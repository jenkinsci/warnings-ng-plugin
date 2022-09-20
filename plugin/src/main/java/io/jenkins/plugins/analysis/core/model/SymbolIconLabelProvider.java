package io.jenkins.plugins.analysis.core.model;

/**
 * Provides SVG icons that have the same name as the URL (or ID) of a tool.
 *
 * @author Ullrich Hafner
 */
public class SymbolIconLabelProvider extends StaticAnalysisLabelProvider {
    private final String symbolName;

    /**
     * Creates a new label provider with the specified ID and name.
     *
     * @param id
     *         the ID (i.e., URL)
     * @param name
     *         the name of the tool
     */
    public SymbolIconLabelProvider(final String id, final String name) {
        this(id, name, EMPTY_DESCRIPTION, id);
    }

    /**
     * Creates a new label provider with the specified ID and name.
     *
     * @param id
     *         the ID (i.e., URL)
     * @param name
     *         the name of the tool
     * @param descriptionProvider
     *         provides additional descriptions for an issue
     */
    public SymbolIconLabelProvider(final String id, final String name, final DescriptionProvider descriptionProvider) {
        this(id, name, descriptionProvider, id);
    }

    /**
     * Creates a new label provider with the specified ID and name.
     *
     * @param id
     *         the ID (i.e., URL)
     * @param name
     *         the name of the tool
     * @param descriptionProvider
     *         provides additional descriptions for an issue
     * @param symbolName
     *         the name of the symbol
     */
    public SymbolIconLabelProvider(final String id, final String name, final DescriptionProvider descriptionProvider,
            final String symbolName) {
        super(id, name, descriptionProvider);

        this.symbolName = symbolName;
    }

    @Override
    public String getSmallIconUrl() {
        return symbolName;
    }

    @Override
    public String getLargeIconUrl() {
        return symbolName;
    }
}
