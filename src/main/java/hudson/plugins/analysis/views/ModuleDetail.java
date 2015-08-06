package hudson.plugins.analysis.views;

import hudson.model.AbstractBuild;
import hudson.model.Run;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.model.MavenModule;

/**
 * Result object to visualize the package statistics of a module.
 *
 * @author Ulli Hafner
 */
public class ModuleDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -1854984151887397361L;
    /** The module to show the details for. */
    private final MavenModule module;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param detailFactory
     *            factory to create detail objects with
     * @param module
     *            the module to show the details for
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param header
     *            header to be shown on detail page
     */
    public ModuleDetail(final Run<?, ?> owner, final DetailFactory detailFactory, final MavenModule module, final String defaultEncoding, final String header) {
        super(owner, detailFactory, module.getAnnotations(), defaultEncoding, header, Hierarchy.MODULE);
        this.module = module;
    }

    @Override
    public String getDisplayName() {
        return module.getName();
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    @Override
    public String getHeader() {
        return getName() + " - " + Messages.ModuleDetail_header() + " " + module.getName();
    }

    /**
     * Returns a tooltip showing the distribution of priorities for the selected
     * package.
     *
     * @param packageName
     *            the package to show the distribution for
     * @return a tooltip showing the distribution of priorities
     */
    public String getToolTip(final String packageName) {
        return module.getPackage(packageName).getToolTip();
    }

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param detailFactory
     *            factory to create detail objects with
     * @param module
     *            the module to show the details for
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param header
     *            header to be shown on detail page
     * @deprecated use {@link #ModuleDetail(Run, DetailFactory, MavenModule, String, String)} instead
     */
    @Deprecated
    public ModuleDetail(final AbstractBuild<?, ?> owner, final DetailFactory detailFactory, final MavenModule module, final String defaultEncoding, final String header) {
        this((Run<?, ?>) owner, detailFactory, module, defaultEncoding, header);
    }
}

