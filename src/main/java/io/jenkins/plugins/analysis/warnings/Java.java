package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.JavacParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;
import hudson.plugins.warnings.WarningsDescriptor;

/**
 * Provides a parser and customized messages for the Java compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Java extends StaticAnalysisTool {
    static final String ID = "java";
    static final String PARSER_NAME = Messages.Warnings_JavaParser_ParserName();
    private static final String JAVA_SMALL_ICON = WarningsDescriptor.IMAGE_PREFIX + ID + "-24x24.png";
    private static final String JAVA_LARGE_ICON = WarningsDescriptor.IMAGE_PREFIX + ID + "-48x48.png";

    @Override
    public JavacParser createParser() {
        return new JavacParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new JavaLabelProvider();
    }

    /** Provides the labels for the static analysis tool. */
    public static class JavaLabelProvider extends DefaultLabelProvider {
        JavaLabelProvider() {
            this(ID, PARSER_NAME);
        }

        /**
         * Creates a new {@link JavaLabelProvider} with the specified ID and name.
         *
         * @param id
         *         the ID
         * @param name
         *         the name of the static analysis tool
         */
        protected JavaLabelProvider(final String id, final String name) {
            super(id, name);
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
