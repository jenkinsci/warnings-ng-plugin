package hudson.plugins.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Defines the properties of a warnings parser that uses a groovy script to
 * parse the warnings log.
 *
 * @author Ulli Hafner
 */
public class GroovyParser {
    private final String name;
    private final String regexp;
    private final String script;

    /**
     * Creates a new instance of {@link GroovyParser}.
     *
     * @param name
     *            the name of the parser
     * @param regexp
     *            the regular expression
     * @param script
     *            the script to map the expression to a warning
     */
    @DataBoundConstructor
    public GroovyParser(final String name, final String regexp, final String script) {
        this.name = name;
        this.regexp = regexp;
        this.script = script;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the regular expression.
     *
     * @return the regular expression
     */
    public String getRegexp() {
        return regexp;
    }

    /**
     * Returns the Groovy script.
     *
     * @return the Groovy script
     */
    public String getScript() {
        return script;
    }
}

