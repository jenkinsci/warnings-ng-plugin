package io.jenkins.plugins.analysis.core.model;

/**
 * Provides icons that have the same name as the URL (or ID) of a tool.
 *
 * @author Ullrich Hafner
 */
public class IconLabelProvider extends StaticAnalysisLabelProvider {
    private static final String ICONS_URL = "/plugin/warnings-ng/icons/";
    
    private final String smallIconUrl;
    private final String largeIconUrl;

    /**
     * Creates a new label provider with the specified ID and name.
     *
     * @param id
     *         the ID (i.e., URL)
     * @param name
     *         the name of the tool
     */
    public IconLabelProvider(final String id, final String name) {
        this(id, name, id);
    }

    /**
     * Creates a new label provider with the specified ID and name.
     *
     * @param id
     *         the ID (i.e., URL)
     * @param name
     *         the name of the tool
     * @param iconName
     *         the the unique name of the icon file
     */
    public IconLabelProvider(final String id, final String name, final String iconName) {
        super(id, name);
        
        smallIconUrl = ICONS_URL + iconName + "-24x24.png";
        largeIconUrl = ICONS_URL + iconName + "-48x48.png";
    }
    
    @Override
    public String getSmallIconUrl() {
        return smallIconUrl;
    }

    @Override
    public String getLargeIconUrl() {
        return largeIconUrl;
    }
} 
