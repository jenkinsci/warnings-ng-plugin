package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import io.jenkins.plugins.analysis.core.model.SymbolIconLabelProvider;
import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.SvgIconLabelProvider;

/**
 * Provides a parser and customized messages for FindBugs.
 *
 * @author Ullrich Hafner
 */
public class SpotBugs extends FindBugs {
    @Serial
    private static final long serialVersionUID = -8773197511353021180L;
    private static final String ID = "spotbugs";

    /** Creates a new instance of {@link SpotBugs}. */
    @DataBoundConstructor
    public SpotBugs() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    @Symbol("spotBugs")
    public static class Descriptor extends FindBugsDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new SymbolIconLabelProvider(getId(), getName(), getDescriptionProvider(), "symbol-spotbugs plugin-warnings-ng");
        }
    }
}
