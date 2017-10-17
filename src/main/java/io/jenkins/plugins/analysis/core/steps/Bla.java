package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Provides customized messages for PMD.
 *
 * @author Ullrich Hafner
 */
public class Bla extends StaticAnalysisTool {
    private final boolean useRankAsPriority;

    /**
     * Creates a new instance of {@link Bla}.
     */
    @DataBoundConstructor
    public Bla(final boolean useRankAsPriority) {
        super("bla");
        this.useRankAsPriority = useRankAsPriority;
    }

    public boolean getUseRankAsPriority() {
        return useRankAsPriority;
    }

    @Override
    public Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException {
        return null; // FIXME: this parser requires an extra parameter
    }

    @Override
    protected String getName() {
        return "Bla";
    }

    /** Descriptor for PMD. */
    @Extension
    public static final StaticAnalysisToolDescriptor D = new StaticAnalysisToolDescriptor(Bla.class);
}
