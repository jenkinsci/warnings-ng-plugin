package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.parser.TaglistParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Taglist Maven Plugin.
 *
 * @author Ullrich Hafner
 */
public class TagList extends ReportScanningTool {
    private static final long serialVersionUID = 2696608544063390368L;

    static final String ID = "taglist";

    /** Creates a new instance of {@link TagList}. */
    @DataBoundConstructor
    public TagList() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new TaglistParserWrapper();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("tagList")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Taglist_ParserName();
        }

        @Override
        public String getPattern() {
            return "**/taglist.xml";
        }

        @Override
        public String getUrl() {
            return "https://www.mojohaus.org/taglist-maven-plugin";
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }

    private static class TaglistParserWrapper extends TaglistParser {
        private static final long serialVersionUID = 173978860195164797L;

        TaglistParserWrapper() {
            // Empty
        }

        @Override
        public Report parse(ReaderFactory readerFactory) throws ParsingException {
            Report report = super.parse(readerFactory);

            report.stream().forEach(issue -> {
                String origFile = issue.getFileName();

                // Guessing at real file name
                String updated = origFile.replace('.', '/').concat(".java");
                issue.setFileName(updated);
            });

            return report;
        }
    }

}
