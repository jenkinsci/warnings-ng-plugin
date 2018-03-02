package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

import edu.hm.hafner.analysis.parser.violations.XmlLintAdapter;

/**
 * Provides a parser and customized messages for XML-Lint.
 *
 * @author Ullrich Hafner
 */
public class XmlLint extends StaticAnalysisTool {
    static final String ID = "xml-lint";

    /** Creates a new instance of {@link XmlLint}. */
    @DataBoundConstructor
    public XmlLint() {
        // empty constructor required for stapler
    }

    @Override
    public XmlLintAdapter createParser() {
        return new XmlLintAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Violations_XmlLint();
        }
    }
}
