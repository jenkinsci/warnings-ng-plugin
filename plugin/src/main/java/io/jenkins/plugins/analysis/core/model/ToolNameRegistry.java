package io.jenkins.plugins.analysis.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import hudson.model.Run;

/**
 * Registry that maps tool IDs to their human-readable names. This is used to display tool names instead of IDs in
 * trend charts and other visualizations.
 *
 * @author Akash Manna
 */
public class ToolNameRegistry {
    private final Map<String, String> idToNameMap;

    /**
     * Creates an empty registry.
     */
    public ToolNameRegistry() {
        this(new HashMap<>());
    }

    /**
     * Creates a registry with the given ID-to-name mapping.
     *
     * @param idToNameMap
     *         the mapping of tool IDs to names
     */
    public ToolNameRegistry(final Map<String, String> idToNameMap) {
        this.idToNameMap = new HashMap<>(idToNameMap);
    }

    /**
     * Creates a registry from the {@link ResultAction}s of a build. Each action provides a tool ID and name, which
     * are stored in the registry for later lookup. Names are HTML-escaped at creation time.
     *
     * @param build
     *         the build that contains the result actions
     *
     * @return a registry containing all tool IDs and HTML-escaped names from the build
     */
    public static ToolNameRegistry fromBuild(final Run<?, ?> build) {
        Map<String, String> mapping = new HashMap<>();
        LabelProviderFactory factory = new LabelProviderFactory();
        for (ResultAction action : build.getActions(ResultAction.class)) {
            String id = action.getId();
            String name = action.getName();
            if (StringUtils.isBlank(name)) {
                name = factory.create(id).getName();
            }
            mapping.put(id, StringEscapeUtils.escapeHtml4(name));
        }
        return new ToolNameRegistry(mapping);
    }

    /**
     * Returns the human-readable name for a tool ID. If the ID is not registered, attempts to look up the name from
     * the {@link LabelProviderFactory}. If that also fails, returns the ID itself. Names returned are already
     * HTML-escaped.
     *
     * @param id
     *         the tool ID
     *
     * @return the HTML-escaped human-readable name, or the escaped ID if no name is found
     */
    public String getName(final String id) {
        if (idToNameMap.containsKey(id)) {
            return idToNameMap.get(id);
        }
        var labelProvider = new LabelProviderFactory().create(id);
        return StringEscapeUtils.escapeHtml4(labelProvider.getName());
    }

    /**
     * Registers a tool ID with its corresponding name. The name will be HTML-escaped before storing.
     *
     * @param id
     *         the tool ID
     * @param name
     *         the human-readable name
     */
    public void register(final String id, final String name) {
        idToNameMap.put(id, StringEscapeUtils.escapeHtml4(name));
    }

    /**
     * Returns whether the registry contains a mapping for the given tool ID.
     *
     * @param id
     *         the tool ID
     *
     * @return {@code true} if the registry contains a mapping for the ID, {@code false} otherwise
     */
    public boolean contains(final String id) {
        return idToNameMap.containsKey(id);
    }

    /**
     * Returns the number of registered tool IDs.
     *
     * @return the number of registered IDs
     */
    public int size() {
        return idToNameMap.size();
    }

    /**
     * Returns the ID-to-name mapping as an immutable map. The names in the returned map are already HTML-escaped.
     *
     * @return an immutable map from tool IDs to HTML-escaped names
     */
    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(idToNameMap);
    }
}
