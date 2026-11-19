package io.jenkins.plugins.analysis.core.model;

import io.jenkins.plugins.analysis.core.model.HotspotsModel.Baseline;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The configuration of a chart of the Warnings tab that is sent by the client as JSON object (e.g., by
 * {@code hotspots-model.js}). Invalid JSON or values that are not a JSON object result in an empty configuration, so
 * all properties fall back to their defaults.
 *
 * @author Ullrich Hafner
 */
final class ChartConfiguration {
    private static final String TOOL_PROPERTY = "tool";
    private static final String BASELINE_PROPERTY = "baseline";

    private final ObjectNode properties;

    /**
     * Creates a new configuration from the specified JSON object.
     *
     * @param json
     *         the configuration as JSON object
     */
    ChartConfiguration(final String json) {
        properties = parse(json);
    }

    private static ObjectNode parse(final String json) {
        var mapper = new ObjectMapper();
        try {
            if (mapper.readTree(json) instanceof ObjectNode node) {
                return node;
            }
            return mapper.createObjectNode();
        } catch (JacksonException exception) {
            return mapper.createObjectNode();
        }
    }

    /**
     * Returns the value of the specified property as string.
     *
     * @param property
     *         the name of the property
     *
     * @return the value of the property, or an empty string if the property is not set
     */
    String getString(final String property) {
        return properties.optional(property).map(value -> value.asString("")).orElse("");
    }

    /**
     * Returns the value of the specified property as integer.
     *
     * @param property
     *         the name of the property
     * @param defaultValue
     *         the value to return if the property is not set or not a number
     *
     * @return the value of the property
     */
    int getInteger(final String property, final int defaultValue) {
        return properties
                .optional(property)
                .map(value -> value.asInt(defaultValue))
                .orElse(defaultValue);
    }

    /**
     * Returns the selected baseline (property {@code baseline}). Defaults to {@link Baseline#ALL}.
     *
     * @return the baseline
     */
    Baseline getBaseline() {
        return Baseline.fromId(getString(BASELINE_PROPERTY)).orElse(Baseline.ALL);
    }

    /**
     * Returns the selected tools (property {@code tool}). If the property does not match the ID of one of the
     * specified tools (e.g., {@value TrendModel#ALL_TOOLS_ID} or an unknown ID), then all tools are returned.
     *
     * @param actions
     *         the available tools
     *
     * @return the selected tools
     */
    List<ResultAction> selectTools(final List<ResultAction> actions) {
        var id = getString(TOOL_PROPERTY);
        var selected =
                actions.stream().filter(action -> action.getId().equals(id)).toList();
        if (selected.isEmpty()) {
            return actions;
        }
        return selected;
    }
}
