package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.MetrowerksCwCompilerParser;
import edu.hm.hafner.analysis.parser.MetrowerksCwLinkerParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningToolSuite;

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
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_MetrowerksCodeWarrior_ParserName();
        }
    }
}

