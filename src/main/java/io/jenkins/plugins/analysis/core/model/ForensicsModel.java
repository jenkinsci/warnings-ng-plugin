package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.forensics.miner.FileStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

/**
 * Provides the dynamic model for the details table that shows the source control file statistics. The model consists of
 * the following parts:
 *
 * <ul>
 * <li>header name for each column</li>
 * <li>width for each column</li>
 * <li>content for each row</li>
 * <li>content for whole table</li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
class ForensicsModel extends DetailsTableModel {
    static final String UNDEFINED = "-";

    private final RepositoryStatistics statistics;

    ForensicsModel(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
            final DescriptionProvider descriptionProvider, final RepositoryStatistics statistics) {
        super(ageBuilder, fileNameRenderer, descriptionProvider);

        this.statistics = statistics;
    }

    @Override
    public List<Integer> getWidths(final Report report) {
        List<Integer> widths = new ArrayList<>();
        widths.add(1);
        widths.add(1);
        widths.add(1);
        widths.add(1);
        widths.add(1);
        widths.add(2);
        widths.add(2);
        return widths;
    }

    @Override
    public List<String> getHeaders(final Report report) {
        List<String> visibleColumns = new ArrayList<>();
        visibleColumns.add(Messages.Table_Column_Details());
        visibleColumns.add(Messages.Table_Column_File());
        visibleColumns.add(Messages.Table_Column_Age());
        visibleColumns.add(Messages.Table_Column_AuthorsSize());
        visibleColumns.add(Messages.Table_Column_CommitsSize());
        visibleColumns.add(Messages.Table_Column_LastCommit());
        visibleColumns.add(Messages.Table_Column_AddedAt());
        return visibleColumns;
    }

    @Override
    protected List<String> getRow(final Report report, final Issue issue, final String description) {
        List<String> columns = new ArrayList<>();
        columns.add(formatDetails(issue, description));
        columns.add(formatFileName(issue));
        columns.add(formatAge(issue));
        if (statistics.contains(issue.getFileName())) {
            FileStatistics result = statistics.get(issue.getFileName());
            columns.add(String.valueOf(result.getNumberOfAuthors()));
            columns.add(String.valueOf(result.getNumberOfCommits()));
            columns.add(String.valueOf(result.getLastModifiedInDays()));
            columns.add(String.valueOf(result.getAgeInDays()));
        }
        else {
            columns.add(UNDEFINED);
            columns.add(UNDEFINED);
            columns.add(UNDEFINED);
            columns.add(UNDEFINED);
        }
        return columns;
    }
}
