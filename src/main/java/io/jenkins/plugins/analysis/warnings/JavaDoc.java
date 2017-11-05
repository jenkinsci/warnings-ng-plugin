package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Issues;

import hudson.Extension;
import hudson.plugins.warnings.parser.AbstractWarningsParser;
import hudson.plugins.warnings.parser.FileWarningsParser;
import hudson.plugins.warnings.parser.Messages;
import hudson.plugins.warnings.parser.ParserRegistry;

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
    public Issues parse(final File file, final String moduleName) throws InvocationTargetException {
        List<AbstractWarningsParser> parsers = ParserRegistry.getParsers("JavaDoc");

        Issues issues = new FileWarningsParser(parsers, getDefaultEncoding()).parseIssues(file, moduleName);
        return withOrigin(issues, ID);
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
