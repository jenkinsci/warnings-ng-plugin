package hudson.plugins.analysis.views;

import java.util.Collection;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.ModelObject;
import hudson.model.Run;

import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Base class for annotation detail objects. Instances of this class could be used for
 * Hudson Stapler objects that contain a subset of annotations.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractAnnotationsDetail extends AnnotationContainer implements ModelObject {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 1750266351592937774L;

    /** Current build as owner of this object. */
    private final Run<?, ?> owner;
    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;

    /** The factory to create detail objects with. */
    private final DetailFactory detailFactory;

    /**
     * Creates a new instance of {@link AbstractAnnotationsDetail}.
     *
     * @param owner
     *            current build as owner of this object
     * @param detailFactory
     *            factory to create detail objects with
     * @param annotations
     *            the set of warnings represented by this object
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param name
     *            the name of this object
     * @param hierarchy
     *            the hierarchy level of this detail object
     */
    public AbstractAnnotationsDetail(final Run<?, ?> owner, final DetailFactory detailFactory, final Collection<FileAnnotation> annotations, final String defaultEncoding, final String name, final Hierarchy hierarchy) {
        super(name, hierarchy);
        this.owner = owner;
        this.detailFactory = detailFactory;
        this.defaultEncoding = defaultEncoding;

        addAnnotations(annotations);
    }

    /**
     * Returns the defined default encoding.
     *
     * @return the default encoding
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    public String getHeader() {
        return getName() + " - " + getDisplayName();
    }

    /**
     * Returns the build as owner of this object.
     *
     * @return the owner
     */
    public final Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns whether this build is the last available build.
     *
     * @return <code>true</code> if this build is the last available build
     */
    public final boolean isCurrent() {
        return owner.getParent().getLastBuild().number == owner.number;
    }

    /**
     * Returns a localized priority name.
     *
     * @param priorityName
     *            priority as String value
     * @return localized priority name
     */
    public String getLocalizedPriority(final String priorityName) {
        return Priority.fromString(priorityName).getLongLocalizedString();
    }

    /**
     * Returns the dynamic result of this module detail view. Depending on the
     * number of packages, one of the following detail objects is returned:
     * <ul>
     * <li>A detail object for a single workspace file (if the module contains
     * only one package).</li>
     * <li>A package detail object for a specified package (in any other case).</li>
     * </ul>
     *
     * @param link
     *            the link to identify the sub page to show
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of this module detail view
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return detailFactory.createDetails(link, owner, getContainer(), defaultEncoding, getDisplayName());
    }

    /**
     * Returns all possible priorities.
     *
     * @return all priorities
     */
    public Priority[] getPriorities() {
        return Priority.values();
    }
}
