package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.MetrowerksCwCompilerParser;
import edu.hm.hafner.analysis.parser.MetrowerksCwLinkerParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Metrowerks CodeWarrior compiler and linker.
 *
 * @author Aykut Yilmaz
 */
public class MetrowerksCodeWarrior extends StaticAnalysisToolSuite {
    private static final long serialVersionUID = 4315389958099766339L;
    static final String ID = "metrowerks";

    /** Creates a new instance of {@link MetrowerksCodeWarrior}. */
    @DataBoundConstructor
    public MetrowerksCodeWarrior() {
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends AbstractParser> getParsers() {
        return asList(new MetrowerksCwCompilerParser(), new MetrowerksCwLinkerParser());
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
            return Messages.Warnings_MetrowerksCodeWarrior_ParserName();
        }
    }
}

