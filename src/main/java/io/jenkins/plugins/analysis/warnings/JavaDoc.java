package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.JavaDocParser;
import io.jenkins.plugins.analysis.core.model.AbstractParserTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.warnings.Java.JavaLabelProvider;

import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;

/**
 * Provides customized messages for the JavaDoc parser.
 *
 * @author Ullrich Hafner
 */
@Extension
public class JavaDoc extends AbstractParserTool {
    private static final String ID = "javadoc";

    @Override
    public Collection<? extends AbstractParser> getParsers() {
        return only(new JavaDocParser());
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new JavaDocLabelProvider();
    }

    /** Provides the labels for the static analysis tool. */
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
