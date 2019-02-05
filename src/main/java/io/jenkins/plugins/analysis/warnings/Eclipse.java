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
    static final String ID = "eclipse";

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
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public String getHelp() {
            return "<p><p>Create an output file that contains Eclipse ECJ output, in either XML or text format.</p>"
                    + "<p>To log in XML format, specify &quot;.xml&quot; as the file extension to the -log argument:</p>"
                    + "<p>"
                    + "<code>java -jar ecj.jar -log &lt;logfile&gt;.xml &lt;other arguments&gt;</code></p>"
                    + "<p>To log in text format, specify any file extension except &quot;.xml&quot; to the -log argument:</p><p><code>java -jar ecj.jar -log &lt;logfile&gt;.log &lt;other arguments&gt;</code></p></p>";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_EclipseParser_ParserName();
        }
    }
}
