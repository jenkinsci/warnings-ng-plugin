package hudson.plugins.warnings.parser;

import javax.annotation.Nonnull;

import org.jvnet.localizer.Localizable;

/**
 * Describes a parser.
 *
 * @author Ullrich Hafner
 * @deprecated use the new analysis-model library
 */
@Deprecated
public class ParserDescription implements Comparable<ParserDescription> {
    private final String group;
    private final Localizable name;

    /**
     * Creates a new instance of {@link ParserDescription}.
     *
     * @param group
     *            the group of the parser
     * @param name
     *            the human readable name of the parser
     */
    public ParserDescription(final String group, final Localizable name) {
        super();
        this.group = group;
        this.name = name;
    }

    /**
     * Returns the group of the parser (ID).
     *
     * @return the group of the parser
     */
    public String getGroup() {
        return group;
    }

    /**
     * Returns whether this parser is in the specified group.
     *
     * @param other
     *            the name of the group
     * @return <code>true</code> if this parser is in the specified group
     */
    public boolean isInGroup(final String other) {
        return ParserRegistry.getParser(group).getGroup().equals(ParserRegistry.getParser(other).getGroup());
    }

    /**
     * Returns the human readable name of the parser (localized).
     *
     * @return the human readable name of the parser
     */
    public String getName() {
        return name.toString();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(@Nonnull final ParserDescription o) {
        return name.toString().compareTo(o.name.toString());
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) { // NOPMD
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ParserDescription other = (ParserDescription)obj;
        if (group == null) {
            if (other.group != null) {
                return false;
            }
        }
        else if (!group.equals(other.group)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}

