package hudson.plugins.warnings.parser;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.plugins.warnings.WarningsDescriptor;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * Creates a warning based on a regular expression match and groovy script.
 *
 * @author Ulli Hafner
 */
public class GroovyExpressionMatcher {
    private final String script;
    private final Warning falsePositive;

    /**
     * Creates a new instance of {@link GroovyExpressionMatcher}.
     *
     * @param script
     *            Groovy script
     * @param falsePositive
     *            indicates a false positive
     */
    public GroovyExpressionMatcher(final String script, final Warning falsePositive) {
        this.script = script;
        this.falsePositive = falsePositive;
    }

    /**
     * Creates a new annotation for the specified match.
     *
     * @param matcher
     *            the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    public Warning createWarning(final Matcher matcher) {
        Binding binding = new Binding();
        binding.setVariable("matcher", matcher);
        GroovyShell shell = new GroovyShell(WarningsDescriptor.class.getClassLoader(), binding);
        Object result = null;
        try {
            result = shell.evaluate(script);
            if (result instanceof Warning) {
                return (Warning)result;
            }
        }
        catch (Exception exception) { // NOCHECKSTYLE: catch all exceptions of the Groovy script
            LOGGER.log(Level.SEVERE, "Groovy dynamic warnings parser: exception during parsing: ", exception);
        }
        return falsePositive;
    }

    private static final Logger LOGGER = Logger.getLogger(GroovyExpressionMatcher.class.getName());
}

