package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.MetrowerksCWCompilerParser;
import edu.hm.hafner.analysis.parser.MetrowerksCWLinkerParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Metrowerks CodeWarrior compiler and linker.
 *
 * @author Aykut Yilmaz
 */
public class MetrowerksCodeWarrior extends StaticAnalysisToolSuite {
    static final String ID = "metrowerks";

    /** Creates a new instance of {@link MetrowerksCodeWarrior}. */
    @DataBoundConstructor
    public MetrowerksCodeWarrior() {
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends AbstractParser<Issue>> getParsers() {
        return asList(new MetrowerksCWCompilerParser(), new MetrowerksCWLinkerParser());
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

