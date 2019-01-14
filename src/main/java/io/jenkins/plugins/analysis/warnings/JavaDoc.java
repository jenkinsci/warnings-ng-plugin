package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.JavaDocParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides customized messages for the JavaDoc parser.
 *
 * @author Ullrich Hafner
 */
public class JavaDoc extends ReportScanningTool {
    private static final long serialVersionUID = -3987566418736570996L;
    static final String ID = "javadoc-warnings";

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
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
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
