package io.jenkins.plugins.analysis.warnings.groovy;

import java.io.Serializable;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.codehaus.groovy.control.CompilationFailedException;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.GroovySandbox;

/**
 * Creates a warning based on a regular expression match and groovy script.
 *
 * @author Ullrich Hafner
 */
class GroovyExpressionMatcher implements Serializable {
    private static final long serialVersionUID = -2218299240520838315L;
    private static final Logger LOGGER = Logger.getLogger(GroovyExpressionMatcher.class.getName());
    private final String script;
    private transient Script compiled;

    /**
     * Creates a new instance of {@link GroovyExpressionMatcher}.
     *
     * @param script
     *         Groovy script
     */
    GroovyExpressionMatcher(final String script) {
        this.script = script;
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
        GroovyShell shell = new GroovyShell(GroovySandbox.createSecureClassLoader(GroovyExpressionMatcher.class.getClassLoader()),
                binding, GroovySandbox.createSecureCompilerConfiguration());
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
     * @param fileName
     *         the name of the parsed report file
     *
     * @return a new annotation for the specified pattern
     */
    @SuppressWarnings("all")
    public Optional<Issue> createIssue(final Matcher matcher, final IssueBuilder builder, final int lineNumber,
            final String fileName) {
        Object result = run(matcher, builder, lineNumber, fileName);
        if (result instanceof Optional) {
            Optional<?> optional = (Optional) result;
            if (optional.isPresent()) {
                Object wrappedIssue = optional.get();
                if (wrappedIssue instanceof Issue) {
                    return Optional.of((Issue)wrappedIssue);
                }
            }
        }
        return Optional.empty();
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
     * @param fileName
     *         the name of the parsed report file
     *
     * @return unchecked result of the script
     */
    public Object run(final Matcher matcher, final IssueBuilder builder, final int lineNumber, final String fileName) {
        if (compileScriptIfNotYetDone()) {
            Binding binding = compiled.getBinding();
            binding.setVariable("matcher", matcher);
            binding.setVariable("builder", builder);
            binding.setVariable("lineNumber", lineNumber);
            binding.setVariable("fileName", fileName);

            return runScript();
        }
        return Optional.empty();
    }

    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private Object runScript() {
        try {
            return compiled.run();
        }
        catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Groovy dynamic warnings parser: exception during execution: ", exception);
            return Optional.empty();
        }
    }
}

