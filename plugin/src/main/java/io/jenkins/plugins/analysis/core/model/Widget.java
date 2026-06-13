package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for displaying the Warnings widget.
 */
public class Widget {
    private final String symbol;
    private final List<String> lines = new ArrayList<>();
    private final List<ResultAction> failedResults;

    /**
     * Constructs a widget.
     * @param result the list of results to display in the widget.
     */
    public Widget(final List<ResultAction> result) {
        failedResults = result.stream().filter(e -> e.getResult().getTotalSize() > 0).toList();
        int failCount = failedResults.stream().map(e -> e.getResult().getTotalSize()).reduce(0, Integer::sum);
        boolean isFailed = failCount > 0;

        this.symbol = isFailed ? "symbol-warning-outline plugin-ionicons-api" : "symbol-status-blue";

        if (isFailed) {
            lines.add(Messages.Widget_WarningsForThisBuild(failCount));
        }
        else {
            lines.add(Messages.Widget_AllClear());
            lines.add(Messages.Widget_NoWarningsForThisBuild());
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public List<String> getLines() {
        return lines;
    }

    public List<ResultAction> getFailedResults() {
        return failedResults;
    }
}
