package hudson.plugins.warnings.util;

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.model.JavaPackage;

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
     * @param javaPackage
     *            the package to show the details for
     * @param header
     *            header to be shown on detail page
     */
    public PackageDetail(final AbstractBuild<?, ?> owner, final JavaPackage javaPackage, final String header) {
        super(owner, javaPackage.getAnnotations(), header, Hierarchy.PACKAGE);
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

    /** {@inheritDoc} */
    public String getDisplayName() {
        return javaPackage.getName();
    }
}

