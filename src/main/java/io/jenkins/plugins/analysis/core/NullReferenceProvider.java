package io.jenkins.plugins.analysis.core;

import javax.annotation.CheckForNull;

import hudson.model.Run;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;

/**
 * Null object pattern for {@link ReferenceProvider} instances.
 *
 * @author Ullrich Hafner
 */
public class NullReferenceProvider implements ReferenceProvider {
    @CheckForNull
    @Override
    public Run<?, ?> getReference() {
        return null;
    }

    @Override
    public boolean hasReference() {
        return false;
    }

    @Override
    public AnnotationContainer getIssues() {
        return new DefaultAnnotationContainer();
    }
}
