package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;

import hudson.Extension;
import hudson.plugins.warnings.WarningsDescriptor;
import hudson.plugins.warnings.parser.AbstractWarningsParser;
import hudson.plugins.warnings.parser.FileWarningsParser;
import hudson.plugins.warnings.parser.Messages;
import hudson.plugins.warnings.parser.ParserRegistry;

/**
 * Provides a parser and customized messages for the Java compiler.
 *
 * @author Ullrich Hafner
 */
public class Java extends StaticAnalysisTool {
    private static final String JAVA_SMALL_ICON = WarningsDescriptor.IMAGE_PREFIX + "java-24x24.png";
    private static final String JAVA_LARGE_ICON = WarningsDescriptor.IMAGE_PREFIX + "java-48x48.png";
    private static final String ID = "java";

    @DataBoundConstructor
    public Java() {
        // empty constructor required for stapler
    }

    @Override
    public Issues parse(final File file, final String moduleName) throws InvocationTargetException {
        List<AbstractWarningsParser> parsers = ParserRegistry.getParsers("Java Compiler");

        // FIXME: use new parsers from model!
        Issues issues = new FileWarningsParser(parsers, getDefaultEncoding()).parseIssues(file, moduleName);
        return withOrigin(issues, ID);
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
            super(ID);
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
