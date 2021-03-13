package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.JavacParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for Kotlin errors and warnings.
 *
 * @author Sladyn Nunes
 */
public class Kotlin extends AnalysisModelParser {
    private static final long serialVersionUID = -8933886588477373744L;
    private static final String ID = "kotlin";

    /**
     * Creates a new instance of {@link Kotlin}.
     */
    @DataBoundConstructor
    public Kotlin() {
        super();
        // empty constructor required for stapler
    }

    /**
     * Descriptor for this static analysis tool.
     */
    @Symbol("kotlin")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /**
         * Creates the descriptor instance.
         */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Kotlin_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName(), getDescriptionProvider());
        }
    }
}
