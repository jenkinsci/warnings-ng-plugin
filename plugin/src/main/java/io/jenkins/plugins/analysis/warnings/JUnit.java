package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for JUnit.
 *
 * @author Gyanesha Prajjwal
 */
public class JUnit extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -5341616371387604827L;
    private static final String ID = "junit";

    /** Creates a new instance of {@link JUnit}. */
    @DataBoundConstructor
    public JUnit() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("junitParser")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public String getUrl() {
            return "https://junit.org";
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName(), getDescriptionProvider());
        }
    }
}
