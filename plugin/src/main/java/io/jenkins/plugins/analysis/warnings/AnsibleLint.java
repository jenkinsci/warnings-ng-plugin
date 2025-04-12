package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.SymbolIconLabelProvider;

/**
 * Provides a parser and customized messages for the Ansible Lint Compiler.
 *
 * @author Ullrich Hafner
 */
public class AnsibleLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -838846658095256811L;
    private static final String ID = "ansiblelint";

    /** Creates a new instance of {@link AnsibleLint}. */
    @DataBoundConstructor
    public AnsibleLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("ansibleLint")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new SymbolIconLabelProvider(getId(), getDisplayName(), getDescriptionProvider(),
                    "symbol-cib-ansible plugin-warnings-ng");
        }
    }
}
