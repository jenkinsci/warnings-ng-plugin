package io.jenkins.plugins.analysis.warnings.checkstyle;

/**
 * Java Bean class representing a DocBook subsection.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.DataClass")
public class Topic {
    /** The name of this topic. */
    private String name;
    /** The value of this topic. */
    private String value;

    /**
     * Returns the name of this topic.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this topic.
     *
     * @param name
     *         the name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the value of this topic.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of this topic.
     *
     * @param value
     *         the value
     */
    public void setValue(final String value) {
        this.value = value;
    }
}
