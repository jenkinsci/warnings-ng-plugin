package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;

/**
 * A {@link StaticAnalysisLabelProvider} that uses a user defined name. All other properties are provided by the
 * provided delegate.
 *
 * @author Ullrich Hafner
 */
public class CompositeLabelProvider extends DefaultLabelProvider {
    private final StaticAnalysisLabelProvider labelProvider;
    private final String name;

    public CompositeLabelProvider(final StaticAnalysisLabelProvider labelProvider, final String name) {
        super(labelProvider.getId(), name);

        this.labelProvider = labelProvider;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSmallIconUrl() {
        return labelProvider.getSmallIconUrl();
    }

    @Override
    public String getLargeIconUrl() {
        return labelProvider.getLargeIconUrl();
    }

    @Override
    public String getDescription(final Issue issue) {
        return labelProvider.getDescription(issue);
    }

    @Override
    public String getTooltip(final int numberOfItems) {
        return labelProvider.getTooltip(numberOfItems);
    }
}
