package hudson.plugins.analysis.views;

import hudson.model.AbstractBuild;
import hudson.model.Run;

import hudson.plugins.analysis.util.model.JavaPackage;

/**
 * Result object to visualize the package statistics of a module.
 *
 * @author Ulli Hafner
 */
public class PackageDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5315146140343619856L;
    /** The package to show the details for. */
    private final JavaPackage javaPackage;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param detailFactory
     *            factory to create detail objects with
     * @param javaPackage
     *            the package to show the details for
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param header
     *            header to be shown on detail page
     */
    public PackageDetail(final Run<?, ?> owner, final DetailFactory detailFactory, final JavaPackage javaPackage, final String defaultEncoding, final String header) {
        super(owner, detailFactory, javaPackage.getAnnotations(), defaultEncoding, header, Hierarchy.PACKAGE);
        this.javaPackage = javaPackage;
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    @Override
    public String getHeader() {
        return getName() + " - " + javaPackage.getPackageCategoryName() + " " + javaPackage.getName();
    }

    @Override
    public String getDisplayName() {
        return javaPackage.getName();
    }

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param detailFactory
     *            factory to create detail objects with
     * @param javaPackage
     *            the package to show the details for
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param header
     *            header to be shown on detail page
     * @deprecated use {@link #PackageDetail(Run, DetailFactory, JavaPackage, String, String)} instead
     */
    @Deprecated
    public PackageDetail(final AbstractBuild<?, ?> owner, final DetailFactory detailFactory, final JavaPackage javaPackage, final String defaultEncoding, final String header) {
        this((Run<?, ?>) owner, detailFactory, javaPackage, defaultEncoding, header);
    }
}

