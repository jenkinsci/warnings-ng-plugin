package hudson.plugins.warnings.parser;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.plugins.warnings.WarningsDescriptor;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * A parser that uses a configurable regular expression and Groovy script to parse warnings.
 *
 * @author Ulli Hafner
 */
public class DynamicParser extends RegexpLineParser {
    private final String script;

    /**
     * Creates a new instance of {@link DynamicParser}.
     *
     * @param name
     *            name of the parser
     * @param regexp
     *            regular expression
     * @param script
     *            Groovy script
     */
    public DynamicParser(final String name, final String regexp, final String script) {
        super(regexp, name, true);

        this.script = script;
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher
     *            the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
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
            LOGGER.log(Level.SEVERE, getName() + ": exception during parsing: ", exception);
        }
        return FALSE_POSITIVE;
    }

    private static final Logger LOGGER = Logger.getLogger(DynamicParser.class.getName());
}

