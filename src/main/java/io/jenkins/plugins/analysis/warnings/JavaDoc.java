package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.parser.JavaDocParser;

import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;

/**
 * Provides customized messages for the JavaDoc parser.
 *
 * @author Ullrich Hafner
 */
public class JavaDoc extends Java {
    private static final String ID = "javadoc";

    @DataBoundConstructor
    public JavaDoc() {
        // empty constructor required for stapler
    }

    @Override
    public Issues<Issue> parse(final File file, final IssueBuilder builder) throws InvocationTargetException {
        return new JavaDocParser().parse(file, builder);
    }

    /** Registers this tool as extension point implementation. */
    @Extension
    public static class Descriptor extends JavaDescriptor {
        public Descriptor() {
            super(new JavaDocLabelProvider());
        }
    }

    public static class JavaDocLabelProvider extends JavaLabelProvider {
        public JavaDocLabelProvider() {
            super(ID);
        }

        @Override
        public String getName() {
            return Messages.Warnings_JavaDoc_ParserName();
        }

        @Override
        public String getLinkName() {
            return Messages.Warnings_JavaDoc_LinkName();
        }

        @Override
        public String getTrendName() {
            return Messages.Warnings_JavaDoc_TrendName();
        }
    }
}
