package hudson.plugins.warnings.util;

import hudson.plugins.warnings.util.model.AnnotationProvider;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.Localizable;

/**
 * A {@link HealthDescriptor} that neither has the failure threshold nor the
 * health report enabled.
 *
 * @author Ulli Hafner
 */
public class NullHealthDescriptor extends AbstractHealthDescriptor {
    /** Null localizable. */
    private static final NullLocalizable NULL_LOCALIZABLE = new NullLocalizable();
    /** Shared null health descriptor. */
    public static final NullHealthDescriptor NULL_HEALTH_DESCRIPTOR = new NullHealthDescriptor();
    /** Unique ID of this class. */
    private static final long serialVersionUID = -4856077818215392075L;

    /** {@inheritDoc} */
    @Override
    protected Localizable createDescription(final AnnotationProvider result) {
        return NULL_LOCALIZABLE;
    }

    /**
     * A null {@link Localizable}.
     */
    private static final class NullLocalizable extends Localizable {
        /**
         * Creates a new instance of {@link NullLocalizable}.
         */
        private NullLocalizable() {
            super(null, null);
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return StringUtils.EMPTY;
        }

        /** {@inheritDoc} */
        @Override
        public String toString(final Locale locale) {
            return toString();
        }
    }
}

