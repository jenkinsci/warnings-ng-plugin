package io.jenkins.plugins.analysis.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Facade for Jackson that does wrap an exception into a {@link RuntimeException}.
 *
 * @author Ullrich Hafner
 */
public class JacksonFacade {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a JSON representation of the specified bean using Jackson data binding.
     *
     * @param bean
     *         the bean to convert
     * @return the JSON representation (as a String)
     */
    public String toJson(final Object bean) {
        try {
            return mapper.writeValueAsString(bean);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                    String.format("Can't convert %s to JSON object", bean), exception);
        }
    }

    /**
     * Creates a new root {@link ArrayNode}.
     *
     * @return the created node
     */
    public ArrayNode createArray() {
        return mapper.createArrayNode();
    }

    /**
     * Creates a new root {@link ObjectNode}.
     *
     * @return the created node
     */
    public ObjectNode createObject() {
        return mapper.createObjectNode();
    }
}
