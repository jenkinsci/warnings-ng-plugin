package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.EclipseMavenParser;
import edu.hm.hafner.analysis.parser.EclipseParser;
import edu.hm.hafner.analysis.parser.EclipseXMLParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningToolSuite;

/**
 * Provides a parser and customized messages for the Eclipse Compiler.
 *
 * @author Ullrich Hafner
 */
public class Eclipse extends ReportScanningToolSuite {
    private static final long serialVersionUID = -2312612497121380654L;
    private static final String ID = "eclipse";

    /** Creates a new instance of {@link Eclipse}. */
    @DataBoundConstructor
    public Eclipse() {
        super();
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends IssueParser> getParsers() {
        return asList(new EclipseParser(), new EclipseMavenParser(), new EclipseXMLParser());
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("eclipse")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_EclipseParser_ParserName();
        }
    }
}
