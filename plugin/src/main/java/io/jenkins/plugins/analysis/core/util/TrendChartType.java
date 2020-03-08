package io.jenkins.plugins.analysis.core.util;

/**
 * Defines the type of trend chart to use.
 */
public enum TrendChartType {
    /** The aggregation trend is shown <b>before</b> all other analysis tool trend charts. */
    AGGREGATION_TOOLS,
    /** The aggregation trend is shown <b>after</b> all other analysis tool trend charts. */
    TOOLS_AGGREGATION,
    /** The aggregation trend is not shown, only the analysis tool trend charts are shown. */
    TOOLS_ONLY,
    /** Neither the aggregation trend nor analysis tool trend charts are shown. */
    NONE
}
