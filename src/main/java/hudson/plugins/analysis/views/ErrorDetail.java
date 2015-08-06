package hudson.plugins.analysis.views;

import java.util.Collection;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.ModelObject;

import hudson.plugins.analysis.Messages;

/**
 * Result object to visualize the errors during execution of the plug-in.
 *
 * @author Ulli Hafner
 */
public class ErrorDetail implements ModelObject  {
    /** Current build as owner of this action. */
    private final Run<?, ?> owner;
    /** All errors of the project. */
    private final Collection<String> errors;

    /**
     * Creates a new instance of <code>ErrorDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param errors
     *            all modules of the project
     */
    public ErrorDetail(final Run<?, ?> owner, final Collection<String> errors) {
        this.owner = owner;
        this.errors = errors;
    }

    /**
     * Returns the build as owner of this action.
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

    @Override
    public String getDisplayName() {
        return Messages.Errors();
    }

    /**
     * Returns the errors in the project.
     *
     * @return the errors in the project
     */
    public Collection<String> getErrors() {
        return errors;
    }

    /**
     * Creates a new instance of <code>ErrorDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param errors
     *            all modules of the project
     * @deprecated use {@link #ErrorDetail(Run, Collection)} instead
     */
    @Deprecated
    public ErrorDetail(final AbstractBuild<?, ?> owner, final Collection<String> errors) {
        this.owner = owner;
        this.errors = errors;
    }
}

