package hudson.plugins.warnings;

import org.jvnet.localizer.Localizable;

import hudson.plugins.analysis.core.AbstractHealthDescriptor;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.util.model.AnnotationProvider;

/**
 * A health descriptor for warnings build results.
 *
 * @author Ulli Hafner
 */
public class WarningsHealthDescriptor extends AbstractHealthDescriptor {
    private static final long serialVersionUID = -3404826986876607396L;
    private Localizable name;

    /**
     * Creates a new instance of {@link WarningsHealthDescriptor} based on the
     * values of the specified descriptor.
     *
     * @param healthDescriptor the descriptor to copy the values from
     * @param name the name of the health report
     */
    public WarningsHealthDescriptor(final HealthDescriptor healthDescriptor, final Localizable name) {
        super(healthDescriptor);
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    protected Object readResolve() {
        if (name == null) {
            name = Messages._Warnings_ProjectAction_Name();
        }
        return super.readResolve();
    }

    @Override
    protected Localizable createDescription(final AnnotationProvider result) {
        if (result.getNumberOfAnnotations() == 0) {
            return Messages._Warnings_ResultAction_HealthReportNoItem(name);
        }
        else if (result.getNumberOfAnnotations() == 1) {
            return Messages._Warnings_ResultAction_HealthReportSingleItem(name);
        }
        else {
            return Messages._Warnings_ResultAction_HealthReportMultipleItem(name, result.getNumberOfAnnotations());
        }
    }
}

