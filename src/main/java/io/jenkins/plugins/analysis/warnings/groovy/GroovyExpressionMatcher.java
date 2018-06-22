package io.jenkins.plugins.analysis.warnings.groovy;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.codehaus.groovy.control.CompilationFailedException;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
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
    private static final Logger LOGGER = Logger.getLogger(GroovyExpressionMatcher.class.getName());
    private final Issue falsePositive;
    private final String script;
    private transient Script compiled;

    /**
     * Creates a new instance of {@link GroovyExpressionMatcher}.
     *
     * @param script
     *         Groovy script
     * @param falsePositive
     *         indicates a false positive
     */
    public GroovyExpressionMatcher(final String script, final Issue falsePositive) {
        this.script = script;
        this.falsePositive = falsePositive;
    }

    private boolean compileScriptIfNotYetDone() {
        synchronized (script) {
            if (compiled == null) {
                try {
                    compiled = compile();
                }
                catch (CompilationFailedException exception) {
                    LOGGER.log(Level.SEVERE, "Groovy dynamic warnings parser: exception during compiling: ", exception);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Compiles the script.
     *
     * @return the compiled script
     * @throws CompilationFailedException
     *         if the script contains compile errors
     */
    public Script compile() throws CompilationFailedException {
        Binding binding = new Binding();
        binding.setVariable("falsePositive", falsePositive);
        GroovyShell shell = new GroovyShell(WarningsDescriptor.class.getClassLoader(), binding);
        return shell.parse(script);
    }

    /**
     * Creates a new issue for the specified match.
     *
     * @param matcher
     *         the regular expression matcher
     * @param builder
     *         the issue builder
     * @param lineNumber
     *         the current line number
     *
     * @return a new annotation for the specified pattern
     */
    @SuppressWarnings("all")
    public Issue createIssue(final Matcher matcher, final IssueBuilder builder, final int lineNumber) {
        Object result = run(matcher, builder, lineNumber);
        if (result instanceof Issue) {
            return (Issue) result;
        }
        return falsePositive;
    }

    /**
     * Runs the groovy script. No exceptions are caught.
     *
     * @param matcher
     *         the regular expression matcher
     * @param builder
     *         the issue builder
     * @param lineNumber
     *         the current line number
     *
     * @return unchecked result of the script
     */
    public Object run(final Matcher matcher, final IssueBuilder builder, final int lineNumber) {
        if (compileScriptIfNotYetDone()) {
            Binding binding = compiled.getBinding();
            binding.setVariable("matcher", matcher);
            binding.setVariable("builder", builder);
            binding.setVariable("lineNumber", lineNumber);

            return runScript();
        }
        return falsePositive;
    }

    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private Object runScript() {
        try {
            return compiled.run();
        }
        catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Groovy dynamic warnings parser: exception during execution: ", exception);
            return falsePositive;
        }
    }

    /**
     * Creates a new issue for the specified match.
     *
     * @param matcher
     *         the regular expression matcher
     * @param builder
     *         the issue builder
     *
     * @return a new annotation for the specified pattern
     */
    public Issue createIssue(final Matcher matcher, final IssueBuilder builder) {
        return createIssue(matcher, builder, 0);
    }
}

