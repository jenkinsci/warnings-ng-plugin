package io.jenkins.plugins.analysis.core.model;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.ArrayList;
import java.util.List;

public class Widget {

    private final String symbol;
    private final List<String> lines = new ArrayList<>();
    private final List<ResultAction> failedResults;

    public Widget(@MonotonicNonNull List<ResultAction> result) {
        failedResults = result.stream().filter(e -> e.getResult().getTotalSize() > 0).toList();
        int failCount = result.stream().map(e -> e.getResult().getTotalSize()).reduce(0, Integer::sum);
        boolean isFailed = failCount > 0;

        this.symbol = isFailed ? "symbol-warning-outline plugin-ionicons-api" : "symbol-status-blue";

        List<String> counts = new ArrayList<>();

        if (isFailed) {
            lines.add(Messages.Widget_WarningsForThisBuild(failCount));
        } else {
            lines.add(Messages.Widget_AllClear());
            lines.add(Messages.Widget_NoWarningsForThisBuild());
        }

        lines.add(String.join(", ", counts));
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
