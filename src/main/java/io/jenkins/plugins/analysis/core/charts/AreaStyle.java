package io.jenkins.plugins.analysis.core.charts;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Area style for a stacked chart.
 * <p>
 * This class will be automatically converted to a JSON object.
 * </p>
 * @author Ullrich Hafner
 */
@SuppressWarnings("FieldCanBeLocal")
public class AreaStyle {
    @SuppressFBWarnings("SS_SHOULD_BE_STATIC")
    private final boolean normal = true;

    AreaStyle() {
    }

    public boolean isNormal() {
        return normal;
    }
}
