package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.JavacParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StreamBasedParser;

import hudson.Extension;
import hudson.plugins.warnings.WarningsDescriptor;
import hudson.plugins.warnings.parser.Messages;

/**
 * Provides a parser and customized messages for the Java compiler.
 *
 * @author Ullrich Hafner
 */
public class Java extends StreamBasedParser {
    private static final String JAVA_SMALL_ICON = WarningsDescriptor.IMAGE_PREFIX + "java-24x24.png";
    private static final String JAVA_LARGE_ICON = WarningsDescriptor.IMAGE_PREFIX + "java-48x48.png";

    @DataBoundConstructor
    public Java() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new JavacParser();
    }

    /** Registers this tool as extension point implementation. */
    @Extension
    public static class JavaDescriptor extends StaticAnalysisToolDescriptor {
        public JavaDescriptor() {
            super(new JavaLabelProvider());
        }

        public JavaDescriptor(final StaticAnalysisLabelProvider labelProvider) {
            super(labelProvider);
        }
    }

    public static class JavaLabelProvider extends DefaultLabelProvider {
        public JavaLabelProvider() {
            super("java");
        }

        public JavaLabelProvider(final String id) {
            super(id);
        }

        @Override
        public String getName() {
            return Messages.Warnings_JavaParser_ParserName();
        }

        @Override
        public String getLinkName() {
            return Messages.Warnings_JavaParser_LinkName();
        }

        @Override
        public String getTrendName() {
            return Messages.Warnings_JavaParser_TrendName();
        }

        @Override
        public String getSmallIconUrl() {
            return JAVA_SMALL_ICON;
        }

        @Override
        public String getLargeIconUrl() {
            return JAVA_LARGE_ICON;
        }
    }
}
