package io.jenkins.plugins.analysis.core.charts;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Area style for a stacked chart.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("FieldCanBeLocal")
public class AreaStyle {
    @SuppressFBWarnings("SS_SHOULD_BE_STATIC")
    private final boolean normal = true;

    public boolean isNormal() {
        return normal;
    }
}
