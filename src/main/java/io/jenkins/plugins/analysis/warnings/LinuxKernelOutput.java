package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.LinuxKernelOutputParser;
import io.jenkins.plugins.analysis.core.model.AbstractParserTool;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the LinuxKernelOutput compiler.
 *
 * @author Johannes Arzt
 */
@Extension
public class LinuxKernelOutput extends AbstractParserTool {
    private static final String ID = "linux";
    private static final String PARSER_NAME = Messages.Warnings_LinuxKernelOutput_ParserName();

    @Override
    public Collection<? extends AbstractParser> getParsers() {
        return only(new LinuxKernelOutputParser());
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new LabelProvider();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super(ID, PARSER_NAME);
        }
    }
}
