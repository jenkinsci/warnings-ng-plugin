package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.dry.cpd.CpdParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for CPD.
 *
 * @author Ullrich Hafner
 */
public class Cpd extends DuplicateCodeScanner {
    private static final long serialVersionUID = -4121571018057432203L;
    static final String ID = "cpd";

    /** Creates a new instance of {@link Cpd}.
     */
    @DataBoundConstructor
    public Cpd() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public CpdParser createParser() {
        return new CpdParser(getHighThreshold(), getNormalThreshold());
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DryLabelProvider {
        LabelProvider() {
            super(ID, Messages.Warnings_CPD_ParserName());
        }
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("cpd")
    @Extension
    public static class Descriptor extends DryDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_CPD_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider();
        }

        @Override
        public String getPattern() {
            return "**/target/cpd.xml";
        }

        @Override
        public String getUrl() {
            return "https://pmd.github.io/latest/pmd_userdocs_cpd.html";
        }
    }
}
