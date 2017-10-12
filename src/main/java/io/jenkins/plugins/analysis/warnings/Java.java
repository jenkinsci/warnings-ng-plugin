package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;

import hudson.Extension;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.warnings.WarningsDescriptor;
import hudson.plugins.warnings.parser.AbstractWarningsParser;
import hudson.plugins.warnings.parser.FileWarningsParser;
import hudson.plugins.warnings.parser.Messages;
import hudson.plugins.warnings.parser.ParserRegistry;

/**
 * Provides customized messages for the Java parser.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Java extends StaticAnalysisTool {
    private static final String JAVA_SMALL_ICON = WarningsDescriptor.IMAGE_PREFIX + "java-24x24.png";
    private static final String JAVA_LARGE_ICON = WarningsDescriptor.IMAGE_PREFIX + "java-48x48.png";

    @DataBoundConstructor
    public Java() {
        this("java");
    }

    protected Java(final String id) {
        super(id);
    }

    @Override
    public Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException {
        List<AbstractWarningsParser> parsers = ParserRegistry.getParsers("Java Compiler");

        return new FileWarningsParser(parsers, getDefaultEncoding()).parse(file, moduleName);
    }

    @Override
    protected String getName() {
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

    @Extension
    public static final StaticAnalysisToolDescriptor D = new StaticAnalysisToolDescriptor(Java.class);
}
