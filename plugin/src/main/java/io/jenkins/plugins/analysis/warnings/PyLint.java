package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.PyLintParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for Pylint.
 *
 * @author Ullrich Hafner
 */
public class PyLint extends ReportScanningTool {
    private static final long serialVersionUID = 4578376477574960381L;
    private static final String ID = "pylint";

    /** Creates a new instance of {@link PyLint}. */
    @DataBoundConstructor
    public PyLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
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

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(ID, Messages.Warnings_PyLint_ParserName(), createDescriptionProvider());
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PyLint_ParserName();
        }

        @Override
        public String getHelp() {
            return "<p>Create a ./pylintrc that contains:"
                    + "<p><code>msg-template={path}:{module}:{line}: [{msg_id}({symbol}), {obj}] {msg}</code></p>"
                    + "</p>"
                    + "<p>Start pylint using the command:"
                    + "<p><code>pylint --rcfile=./pylintrc CODE > pylint.log</code></p>"
                    + "</p>";
        }
    }
}
