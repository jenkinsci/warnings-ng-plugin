package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.JavacParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;
import hudson.plugins.warnings.WarningsDescriptor;

/**
 * Provides a parser and customized messages for the Java compiler.
 *
 * @author Ullrich Hafner
 */
public class Java extends StaticAnalysisTool {
    private static final long serialVersionUID = 2254154391638811877L;
    static final String ID = "java";
    private static final String JAVA_SMALL_ICON = WarningsDescriptor.IMAGE_PREFIX + ID + "-24x24.png";
    private static final String JAVA_LARGE_ICON = WarningsDescriptor.IMAGE_PREFIX + ID + "-48x48.png";

    /** Creates a new instance of {@link NagFortran}. */
    @DataBoundConstructor
    public Java() {
        // empty constructor required for stapler
    }

    @Override
    public JavacParser createParser() {
        return new JavacParser();
    }

    /** Provides the labels for the static analysis tool. */
    public static class JavaLabelProvider extends StaticAnalysisLabelProvider {
        JavaLabelProvider() {
            this(ID, Messages.Warnings_JavaParser_ParserName());
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

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_JavaParser_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new JavaLabelProvider();
        }
    }
}
