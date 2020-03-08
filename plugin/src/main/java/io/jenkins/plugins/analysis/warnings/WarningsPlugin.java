package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.JsonLogParser;
import edu.hm.hafner.analysis.parser.JsonParser;
import edu.hm.hafner.analysis.parser.XmlParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningToolSuite;

import static j2html.TagCreator.*;

/**
 * Provides a parser for the native format of the Warnings Next Generation Plugin.
 *
 * @author Ullrich Hafner
 */
public class WarningsPlugin extends ReportScanningToolSuite {
    private static final long serialVersionUID = 8110398783405047555L;
    private static final String ID = "issues";

    /** Creates a new instance of {@link WarningsPlugin}. */
    @DataBoundConstructor
    public WarningsPlugin() {
        super();
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends IssueParser> getParsers() {
        return asList(new XmlParser("/report/issue"), new JsonLogParser(), new JsonParser());
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("issues")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_WarningsPlugin_ParserName();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public String getHelp() {
            return p().withText("Create an output file that contains issues in the native Warnings Plugin format, "
                    + "in either XML or JSON. The supported format is identical to the format of the remote API calls. "
                    + "The parser is even capable of reading individual lines of a log file that contains issues in JSON format.").render();
        }

        @Override
        public String getUrl() {
            return "https://github.com/jenkinsci/warnings-ng-plugin/blob/master/doc/Documentation.md#export-your-issues-into-a-supported-format";
        }
    }
}
