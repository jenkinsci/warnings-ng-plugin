package hudson.plugins.warnings.util;

import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.warnings.util.model.AnnotationContainer;
import hudson.plugins.warnings.util.model.AnnotationProvider;
import hudson.plugins.warnings.util.model.AnnotationStream;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.XStream;

/**
 * A base class for build results that is capable of storing a reference to the
 * current build. Provides support for persisting the results of the build.
 *
 * @author Ulli Hafner
 */
public abstract class BuildResult implements ModelObject, Serializable, AnnotationProvider {
    /** Unique ID of this class. */
    private static final long serialVersionUID = 1110545450292087475L;
    /** Serialization provider. */
    protected static final XStream XSTREAM = new AnnotationStream();

    /** Current build as owner of this action. */
    private final AbstractBuild<?, ?> owner;
    /** All parsed modules. */
    private Set<String> modules;
    /** The total number of parsed modules (regardless if there are annotations). */
    private final int numberOfModules;

    /**
     * Creates a new instance of {@link BuildResult}.
     *
     * @param build
     *            owner of this result
     * @param modules
     *            the modules represented by this result
     */
    public BuildResult(final AbstractBuild<?, ?> build, final Set<String> modules) {
        owner = build;
        numberOfModules = modules.size();
        this.modules = new HashSet<String>(modules);
    }

    /**
     * Initializes the modules set if not yet serialized.
     *
     * @return the created object
     */
    private Object readResolve() {
        if (modules == null) {
            modules = new HashSet<String>();
        }
        return this;
    }

    /**
     * Returns the modules of this build result.
     *
     * @return the modules
     */
    public Collection<String> getModules() {
        return modules;
    }

    /**
     * Returns the number of modules in this project.
     *
     * @return the number of modules
     */
    public int getNumberOfModules() {
        return numberOfModules;
    }

    /**
     * Returns the serialization file.
     *
     * @return the serialization file.
     */
    protected final XmlFile getDataFile() {
        return new XmlFile(XSTREAM, new File(getOwner().getRootDir(), getSerializationFileName()));
    }

    /**
     * Returns the name of the file to store the serialized annotations.
     *
     * @return the name of the file to store the serialized annotations
     */
    protected abstract String getSerializationFileName();

    /**
     * Returns whether this result belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public final boolean isCurrent() {
        return getOwner().getProject().getLastBuild().number == getOwner().number;
    }

    /**
     * Returns the build as owner of this action.
     *
     * @return the owner
     */
    public final AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    /** {@inheritDoc} */
    public boolean hasAnnotations(final Priority priority) {
        return getContainer().hasAnnotations(priority);
    }

    /** {@inheritDoc} */
    public boolean hasAnnotations(final String priority) {
        return getContainer().hasAnnotations(priority);
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations() {
        return getContainer().hasAnnotations();
    }

    /** {@inheritDoc} */
    public boolean hasNoAnnotations() {
        return getContainer().hasNoAnnotations();
    }

    /** {@inheritDoc} */
    public boolean hasNoAnnotations(final Priority priority) {
        return getContainer().hasAnnotations(priority);
    }

    /** {@inheritDoc} */
    public boolean hasNoAnnotations(final String priority) {
        return getContainer().hasAnnotations(priority);
    }

    /** {@inheritDoc} */
    public Collection<FileAnnotation> getAnnotations() {
        return getContainer().getAnnotations();
    }

    /** {@inheritDoc} */
    public FileAnnotation getAnnotation(final long key) {
        return getContainer().getAnnotation(key);
    }

    /** {@inheritDoc} */
    public FileAnnotation getAnnotation(final String key) {
        return getContainer().getAnnotation(key);
    }

    /** {@inheritDoc} */
    public Collection<FileAnnotation> getAnnotations(final Priority priority) {
        return getContainer().getAnnotations(priority);
    }

    /** {@inheritDoc} */
    public Collection<FileAnnotation> getAnnotations(final String priority) {
        return getContainer().getAnnotations(priority);
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations(final String priority) {
        return getNumberOfAnnotations(Priority.fromString(priority));
    }

    /**
     * Gets the annotation container.
     *
     * @return the container
     */
    public abstract AnnotationContainer getContainer();
}
