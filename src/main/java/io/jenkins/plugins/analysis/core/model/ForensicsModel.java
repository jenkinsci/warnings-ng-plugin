package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.datatables.api.TableColumn;
import io.jenkins.plugins.datatables.api.TableColumn.ColumnCss;
import io.jenkins.plugins.forensics.miner.FileStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

/**
 * Provides the dynamic model for the details table that shows the source control file statistics.
 *
 * <p>
 * This forensics model consists of the following columns:
 * </p>
 * <ul>
 * <li>issue details (message and description)</li>
 * <li>file name</li>
 * <li>age</li>
 * <li>total number of different authors</li>
 * <li>total number of commits</li>
 * <li>time of last commit</li>
 * <li>time of first commit</li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
public class ForensicsModel extends DetailsTableModel {
    static final String UNDEFINED = "-";

    private final RepositoryStatistics statistics;

    ForensicsModel(final Report report, final RepositoryStatistics statistics,
            final FileNameRenderer fileNameRenderer, final AgeBuilder ageBuilder,
            final DescriptionProvider labelProvider) {
        super(report, fileNameRenderer, ageBuilder, labelProvider);

        this.statistics = statistics;
    }

    @Override
    public String getId() {
        return "forensics";
    }

    @Override
    public List<TableColumn> getColumns() {
        List<TableColumn> columns = new ArrayList<>();

        columns.add(createDetailsColumn());
        columns.add(createFileColumn().setWidth(2));
        columns.add(createAgeColumn());
        columns.add(new TableColumn(Messages.Table_Column_AuthorsSize(), "authorsSize"));
        columns.add(new TableColumn(Messages.Table_Column_CommitsSize(), "commitsSize"));
        columns.add(new TableColumn(Messages.Table_Column_LastCommit(), "modifiedAt")
                .setWidth(2)
                .setHeaderClass(ColumnCss.DATE));
        columns.add(new TableColumn(Messages.Table_Column_AddedAt(), "addedAt")
                .setWidth(2)
                .setHeaderClass(ColumnCss.DATE));

        return columns;
    }

    @Override
    public ForensicsRow getRow(final Issue issue) {
        ForensicsRow row = new ForensicsRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(),
                issue);
        if (statistics.contains(issue.getFileName())) {
            FileStatistics result = statistics.get(issue.getFileName());
            row.setAuthorsSize(String.valueOf(result.getNumberOfAuthors()));
            row.setCommitsSize(String.valueOf(result.getNumberOfCommits()));
            row.setModifiedAt(result.getLastModificationTime());
            row.setAddedAt(result.getCreationTime());
        }
        else {
            row.setAuthorsSize(UNDEFINED);
            row.setCommitsSize(UNDEFINED);
            row.setModifiedAt(0);
            row.setAddedAt(0);
        }
        return row;
    }

    /**
     * A table row that shows the source control statistics.
     */
    @SuppressWarnings("PMD.DataClass") // Used to automatically convert to JSON object
    public static class ForensicsRow extends TableRow {
        private String authorsSize;
        private String commitsSize;
        private int modifiedAt;
        private int addedAt;

        ForensicsRow(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
                final DescriptionProvider descriptionProvider, final Issue issue) {
            super(ageBuilder, fileNameRenderer, descriptionProvider, issue);
        }

        public String getAuthorsSize() {
            return authorsSize;
        }

        public String getCommitsSize() {
            return commitsSize;
        }

        public int getModifiedAt() {
            return modifiedAt;
        }

        public int getAddedAt() {
            return addedAt;
        }

        void setAuthorsSize(final String authorsSize) {
            this.authorsSize = authorsSize;
        }

        void setCommitsSize(final String commitsSize) {
            this.commitsSize = commitsSize;
        }

        void setModifiedAt(final int modifiedAt) {
            this.modifiedAt = modifiedAt;
        }

        void setAddedAt(final int addedAt) {
            this.addedAt = addedAt;
        }
    }
}
