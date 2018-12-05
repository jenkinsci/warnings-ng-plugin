package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.AntJavacParser;
import edu.hm.hafner.analysis.parser.JavacParser;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningToolSuite;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Java compiler.
 *
 * @author Ullrich Hafner
 */
public class Java extends ReportScanningToolSuite {
    private static final long serialVersionUID = 2254154391638811877L;
    static final String ID = "java";

    /** Creates a new instance of {@link NagFortran}. */
    @DataBoundConstructor
    public Java() {
        super();
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends IssueParser> getParsers() {
        return asList(new JavacParser(), new AntJavacParser());
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("java")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_JavaParser_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }
    }
}
