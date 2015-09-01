package hudson.plugins.analysis.core;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Set;

import hudson.model.AbstractBuild;

import hudson.model.Run;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Empty build history.
 *
 * @author Ulli Hafner
 */
public class NullBuildHistory extends BuildHistory {
    /**
     * Creates a new instance of {@link NullBuildHistory}.
     */
    public NullBuildHistory() {
        super((Run<?, ?>)null, null, false, false);
    }

    @Override
    public ResultAction<? extends BuildResult> getBaseline() {
        return null;
    }

    @Override
    public Calendar getTimestamp() {
        return new GregorianCalendar();
    }

    @Override
    public AnnotationContainer getReferenceAnnotations() {
        return new DefaultAnnotationContainer();
    }

    @Override
    public AbstractBuild<?, ?> getReferenceBuild() {
        return null;
    }

    @Override
    public boolean hasReferenceBuild() {
        return false;
    }

    @Override
    public boolean hasPreviousResult() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public BuildResult getPreviousResult() {
        return null;
    }

    @Override
    public Collection<FileAnnotation> getNewWarnings(final Set<FileAnnotation> annotations) {
        return annotations;
    }

    @Override
    public Collection<FileAnnotation> getFixedWarnings(final Set<FileAnnotation> annotations) {
        return Collections.emptyList();
    }

    @Override
    public AbstractHealthDescriptor getHealthDescriptor() {
        return new NullHealthDescriptor();
    }
}

