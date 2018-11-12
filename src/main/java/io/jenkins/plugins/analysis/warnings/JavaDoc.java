package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.JavaDocParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides customized messages for the JavaDoc parser.
 *
 * @author Ullrich Hafner
 */
public class JavaDoc extends ReportScanningTool {
    private static final long serialVersionUID = -3987566418736570996L;
    static final String ID = "javadoc";

    /** Creates a new instance of {@link JavaDoc}. */
    @DataBoundConstructor
    public JavaDoc() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public JavaDocParser createParser() {
        return new JavaDocParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("javaDoc")
    @Extension
    public static class Descriptor extends ReportingToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_JavaDoc_ParserName();
        }
       
        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName(), "java");
        }
    }
}
