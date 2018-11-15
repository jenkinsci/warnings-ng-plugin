package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.PyLintParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PyLint.
 *
 * @author Ullrich Hafner
 */
public class PyLint extends ReportScanningTool {
    private static final long serialVersionUID = 4578376477574960381L;
    static final String ID = "pylint";

    /** Creates a new instance of {@link PyLint}. */
    @DataBoundConstructor
    public PyLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public PyLintParser createParser() {
        return new PyLintParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("pyLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PyLint_ParserName();
        }

        @Override
        public String getHelp() {
            return "<p>Create a ./pylintrc that contains:" 
                    + "<p><code>msg-template={path}:{line}: [{msg_id}, {obj}] {msg} ({symbol})</code></p>" 
                    + "</p>" 
                    + "<p>Start pylint using the command:" 
                    + "<p><code>pylint --rcfile=./pylintrc CODE > pylint.log</code></p>" 
                    + "</p>";
        }
    }
}
