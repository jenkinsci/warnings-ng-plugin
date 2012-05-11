package hudson.plugins.warnings;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.warnings.parser.ParserRegistry;

import org.jvnet.localizer.Localizable;

/**
 * Controls the live cycle of the warnings results. This action persists the
 * results of the warnings analysis of a build and displays the results on the
 * build page. The actual visualization of the results is defined in the
 * matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the warnings result trend.
 * </p>
 *
 * @author Ulli Hafner
 */
public class WarningsResultAction extends AbstractResultAction<WarningsResult> {
    private final String parserName;

    /**
     * Creates a new instance of <code>WarningsResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param result
     *            the result in this build
     * @param parserName the name of the parser
     */
    public WarningsResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final WarningsResult result, final String parserName) {
        super(owner, new WarningsHealthDescriptor(healthDescriptor), result);

        this.parserName = parserName;
    }

    @Override
    public String getUrlName() {
        return WarningsDescriptor.getResultUrl(parserName);
    }

    /**
     * Returns the parser group this result belongs to.
     *
     * @return the parser group
     */
    public String getParser() {
        return parserName;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return ParserRegistry.getParser(parserName).getLinkName().toString();
    }

    @Override
    protected String getSmallImage() {
        return ParserRegistry.getParser(parserName).getSmallImage();
    }

    @Override
    public String getLargeImageName() {
        return ParserRegistry.getParser(parserName).getLargeImage();
    }

    @Override
    protected PluginDescriptor getDescriptor() {
        return new WarningsDescriptor();
    }

    @SuppressWarnings("unused")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
    private transient Localizable actionName;
}
