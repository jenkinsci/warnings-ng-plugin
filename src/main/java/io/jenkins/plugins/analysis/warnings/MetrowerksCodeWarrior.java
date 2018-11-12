package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.MetrowerksCwCompilerParser;
import edu.hm.hafner.analysis.parser.MetrowerksCwLinkerParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningToolSuite;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Metrowerks CodeWarrior compiler and linker.
 *
 * @author Aykut Yilmaz
 */
public class MetrowerksCodeWarrior extends ReportScanningToolSuite {
    private static final long serialVersionUID = 4315389958099766339L;
    static final String ID = "metrowerks";

    /** Creates a new instance of {@link MetrowerksCodeWarrior}. */
    @DataBoundConstructor
    public MetrowerksCodeWarrior() {
        super();
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends IssueParser> getParsers() {
        return asList(new MetrowerksCwCompilerParser(), new MetrowerksCwLinkerParser());
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("metrowerksCodeWarrior")
    @Extension
    public static class Descriptor extends ReportingToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_MetrowerksCodeWarrior_ParserName();
        }
    }
}

