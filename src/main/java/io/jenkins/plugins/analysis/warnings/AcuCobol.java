package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.AcuCobolParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;

import hudson.Extension;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.warnings.parser.AbstractWarningsParser;
import hudson.plugins.warnings.parser.FileWarningsParser;
import hudson.plugins.warnings.parser.ParserRegistry;

/**
 * Provides a parser and customized messages for the AcuCobol Compiler.
 *
 * @author Ullrich Hafner
 */
public class AcuCobol extends StaticAnalysisTool {
    private static final String PARSER_NAME = Messages.Warnings_AcuCobol_ParserName();

    @DataBoundConstructor
    public AcuCobol() {
        // empty constructor required for stapler
    }

    @Override
    public Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException {
        List<AbstractWarningsParser> parsers = ParserRegistry.getParsers(PARSER_NAME);

        return new FileWarningsParser(parsers, getDefaultEncoding()).parse(file, moduleName);
    }

    /** Registers this tool as extension point implementation. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new LabelProvider());
        }
    }

    /** Provides the labels for the parser. */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super(new AcuCobolParser().getId(), PARSER_NAME);
        }
    }
}
