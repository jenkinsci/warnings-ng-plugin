package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
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
        return new TaglistParser();
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
    }
}
