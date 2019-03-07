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

        @Override
        public String getHelp() {
            return "<p><p>Ensure that the output from the CodeWarrior build tools is in the expected format. "
                    + "If there are warnings present, but they are not found, then it is likely that the format is incorrect. "
                    + "The mwccarm compiler and mwldarm linker tools may support a configurable message style. "
                    + "This can be used to enforce the expected output format, which may be different from Metrowerks "
                    + "CodeWarrior (and thus require a different tool). For example the following could be appended to "
                    + "the build flags:</p>"
                    + "<p><code>-msgstyle gcc -nowraplines</code></p></p>";
        }
    }
}

