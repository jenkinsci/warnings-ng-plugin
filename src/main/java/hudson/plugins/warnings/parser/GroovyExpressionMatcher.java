package hudson.plugins.warnings.parser;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.codehaus.groovy.control.CompilationFailedException;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import hudson.plugins.warnings.WarningsDescriptor;

/**
 * Creates a warning based on a regular expression match and groovy script.
 *
 * @author Ulli Hafner
 */
public class GroovyExpressionMatcher implements Serializable {
    private static final long serialVersionUID = -2218299240520838315L;

    private final Warning falsePositive;
    private final String script;

    private transient Script compiled;


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

    private void compileScriptIfNotYetDone() {
        synchronized (script) {
             if (compiled == null) {
                 GroovyShell shell = new GroovyShell(WarningsDescriptor.class.getClassLoader());
                 try {
                     compiled = shell.parse(script);
                 }
                 catch (CompilationFailedException exception) {
                     LOGGER.log(Level.SEVERE, "Groovy dynamic warnings parser: exception during compiling: ", exception);
                 }
            }
        }
    }

    /**
     * Creates a new annotation for the specified match.
     *
     * @param matcher
     *            the regular expression matcher
     * @param lineNumber
     *            the current line number
     * @return a new annotation for the specified pattern
     */
    public Warning createWarning(final Matcher matcher, final int lineNumber) {
        compileScriptIfNotYetDone();

        Binding binding = new Binding();
        binding.setVariable("matcher", matcher);
        binding.setVariable("lineNumber", lineNumber);
        Object result = null;
        try {
            compiled.setBinding(binding);
            result = compiled.run();
            if (result instanceof Warning) {
                return (Warning)result;
            }
        }
        catch (Exception exception) { // NOPMD NOCHECKSTYLE: catch all exceptions of the Groovy script
            LOGGER.log(Level.SEVERE, "Groovy dynamic warnings parser: exception during parsing: ", exception);
        }
        return falsePositive;
    }

    /**
     * Creates a new annotation for the specified match.
     *
     * @param matcher
     *            the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    public Warning createWarning(final Matcher matcher) {
        return createWarning(matcher, 0);
    }

    private static final Logger LOGGER = Logger.getLogger(GroovyExpressionMatcher.class.getName());
}

