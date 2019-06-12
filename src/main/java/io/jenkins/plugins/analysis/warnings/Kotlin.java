package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.JavacParser;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Provides a parser and customized messages for PEP8 Python style guide.
 *
 * @author Sladyn Nunes
 */

public class Kotlin extends ReportScanningTool {

    private static final long  serialVersionUID = 00; // To be added
    private static final String ID = "kotlin";

    /** Creates a new instance of {@link Kotlin}. */
    @DataBoundConstructor
    public Kotlin() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new JavacParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("kotlin")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_JavaParser_ParserName();
        }
    }
}
