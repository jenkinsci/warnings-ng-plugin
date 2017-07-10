package hudson.plugins.analysis.views;

import java.io.IOException;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.core.GlobalSettings;
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

    public boolean useAuthors() {
        return !GlobalSettings.instance().getNoAuthors();
    }

    public String getBuildUrl(final int buildNumber) {
        int backward = StringUtils.countMatches(getUrl(), "/");
        return StringUtils.repeat("../", backward + 2) + buildNumber;
    }

    public int getAge(final int buildNumber) {
        return getOwner().getNumber() - buildNumber + 1;
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
    @WithBridgeMethods(value=AbstractBuild.class, adapterMethod="getAbstractBuild")
    public final Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Added for backward compatibility. It generates <pre>AbstractBuild getOwner()</pre> bytecode during the build
     * process, so old implementations can use that signature.
     * 
     * @see {@link WithBridgeMethods}
     */
    @Deprecated
    private final Object getAbstractBuild(Run owner, Class targetClass) {
      return owner instanceof AbstractBuild ? (AbstractBuild) owner : null;
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
        try {
            return detailFactory.createDetails(link, owner, getContainer(), defaultEncoding, getDisplayName());
        }
        catch (NoSuchElementException exception) {
            try {
                response.sendRedirect2("../");
            }
            catch (IOException e) {
                // ignore
            }
            return this; // fallback on broken URLs
        }
    }

    /**
     * Returns all possible priorities.
     *
     * @return all priorities
     */
    public Priority[] getPriorities() {
        return Priority.values();
    }

    public boolean showAuthors() {
    return !GlobalSettings.instance().getNoAuthors();
    }

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
     * @deprecated use {@link #AbstractAnnotationsDetail(Run, DetailFactory, Collection, String, String, Hierarchy)} instead
     */
    @Deprecated
    public AbstractAnnotationsDetail(final AbstractBuild<?, ?> owner, final DetailFactory detailFactory, final Collection<FileAnnotation> annotations, final String defaultEncoding, final String name, final Hierarchy hierarchy) {
        this((Run<?, ?>) owner, detailFactory, annotations, defaultEncoding, name, hierarchy);
    }

    public String getUrl() {
        return StringUtils.EMPTY;
    }
}
