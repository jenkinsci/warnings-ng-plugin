package io.jenkins.plugins.analysis.core.charts;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Area style for a stacked chart.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings("SS_SHOULD_BE_STATIC")
@SuppressWarnings("FieldCanBeLocal")
public class AreaStyle {
    private final boolean normal = true;

    public boolean isNormal() {
        return normal;
    }
}
