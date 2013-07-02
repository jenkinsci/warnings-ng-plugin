package hudson.plugins.warnings;

import org.jvnet.localizer.Localizable;

import hudson.model.AbstractBuild;

import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.PluginDescriptor;

/**
 * FIXME: Document type WarningsTotalResultAction.
 *
 * @author Ulli Hafner
 */
public class WarningsTotalResultAction extends AbstractResultAction<WarningsTotalResult>{

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
  public WarningsTotalResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final WarningsTotalResult result) {
      super(owner, new WarningsHealthDescriptor(healthDescriptor), result);
  }

  @Override
  public String getUrlName() {
      return "warningsTotalResult";
  }

  /**
   * Returns the parser name.
   *
   * @return parser name
   */
  public String getParser() {
      return "totalParsers";
  }


  /** {@inheritDoc} */
  public String getDisplayName() {
      return "All parser warnings";
  }

  @Override
  protected PluginDescriptor getDescriptor() {
      return new WarningsDescriptor();
  }

  @SuppressWarnings("PMD")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("")
  private transient Localizable actionName;

}
