package io.jenkins.plugins.analysis.core.model;

/**
 * Provides SVG icons that have the same name as the URL (or ID) of a tool.
 *
 * @author Ullrich Hafner
 */
public class SvgIconLabelProvider extends StaticAnalysisLabelProvider {
    private static final String ICONS_URL = "/plugin/warnings-ng/icons/";
    private static final String SVG_SUFFIX = ".svg";
    private final String iconUrl;

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
    public SvgIconLabelProvider(final String id, final String name, final DescriptionProvider descriptionProvider) {
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
     * @param iconName
     *         the unique name of the icon file
     */
    public SvgIconLabelProvider(final String id, final String name, final DescriptionProvider descriptionProvider,
            final String iconName) {
        super(id, name, descriptionProvider);

        iconUrl = ICONS_URL + iconName + SVG_SUFFIX;
    }

    @Override
    public String getSmallIconUrl() {
        return iconUrl;
    }

    @Override
    public String getLargeIconUrl() {
        return iconUrl;
    }
}
