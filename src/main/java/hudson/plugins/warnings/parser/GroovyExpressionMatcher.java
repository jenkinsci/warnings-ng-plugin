package hudson.plugins.warnings.parser;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.codehaus.groovy.control.CompilationFailedException;
import org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException;
import org.jenkinsci.plugins.scriptsecurity.sandbox.Whitelist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.GroovySandbox;

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
                 try {
                     compiled = compile();
                 }
                 catch (CompilationFailedException exception) {
                     LOGGER.log(Level.SEVERE, "Groovy dynamic warnings parser: exception during compiling: ", exception);
                 }
            }
        }
    }

    /**
     * Compiles the script.
     *
     * @return the compiled script
     * @throws CompilationFailedException if the script contains compile errors
     */
    public Script compile() throws CompilationFailedException {
        ClassLoader loader = GroovySandbox.createSecureClassLoader(WarningsDescriptor.class.getClassLoader());
        Binding binding = new Binding();
        binding.setVariable("falsePositive", falsePositive);
        GroovyShell shell = new GroovyShell(loader, binding, GroovySandbox.createSecureCompilerConfiguration());
        return shell.parse(script);
    }

    /**
     * Creates a new annotation for the specified match.
     *
     * @param matcher
     *            the regular expression matcher
     * @param lineNumber
     *            the current line number
     * @return a new annotation for the specified pattern
     * @throws RejectedAccessException if the Groovy sandbox rejected the parsing script
     */
    public Warning createWarning(final Matcher matcher, final int lineNumber) throws RejectedAccessException {
        try {
            Object result = run(matcher, lineNumber);
            if (result instanceof Warning) {
                return (Warning)result;
            }
        }
        catch (RejectedAccessException exception) {
            throw exception; // Groovy sandbox rejected the parsing script: needs to be presented to the user
        }
        catch (Exception exception) { // NOPMD NOCHECKSTYLE: catch all exceptions of the Groovy script
            LOGGER.log(Level.SEVERE, "Groovy dynamic warnings parser: exception during execution: ", exception);
        }
        return falsePositive;
    }

    /**
     * Runs the groovy script. No exceptions are caught.
     *
     * @param matcher
     *            the regular expression matcher
     * @param lineNumber
     *            the current line number
     * @return unchecked result of the script
     * @throws RejectedAccessException if the Groovy sandbox rejected the parsing script
     */
    public Object run(final Matcher matcher, final int lineNumber) throws RejectedAccessException {
        compileScriptIfNotYetDone();

        Binding binding = compiled.getBinding();
        binding.setVariable("matcher", matcher);
        binding.setVariable("lineNumber", lineNumber);

        try {
            return GroovySandbox.runInSandbox(new Callable<Object>() {
                @Override public Object call() throws Exception {
                    return compiled.run();
                }
            }, Whitelist.all());
        }
        catch (RejectedAccessException exception) {
            throw exception; // Groovy sandbox rejected the parsing script: needs to be presented to the user
        }
        catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Groovy dynamic warnings parser: exception during execution: ", exception);
            return falsePositive;
        }
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

