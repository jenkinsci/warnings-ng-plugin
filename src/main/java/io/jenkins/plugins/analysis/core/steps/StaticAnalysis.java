package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import hudson.Extension;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
@Extension
public class StaticAnalysis extends StaticAnalysisTool {
    public StaticAnalysis() {
        super("staticAnalysis");
    }

    @Override
    public Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException {
        throw new UnsupportedOperationException("Static analysis generic tool does not support parsing");
    }
}
