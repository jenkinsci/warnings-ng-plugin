package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableColumn.ColumnBuilder;
import io.jenkins.plugins.datatables.TableColumn.ColumnCss;
import io.jenkins.plugins.datatables.TableColumn.ColumnType;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.util.JenkinsFacade;

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
 * <li>lines of code</li>
 * <li>code churn</li>
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
        this(report, statistics, fileNameRenderer, ageBuilder, labelProvider, new JenkinsFacade());
    }

    @VisibleForTesting
    ForensicsModel(final Report report, final RepositoryStatistics statistics,
            final FileNameRenderer fileNameRenderer, final AgeBuilder ageBuilder,
            final DescriptionProvider labelProvider, final JenkinsFacade jenkinsFacade) {
        super(report, fileNameRenderer, ageBuilder, labelProvider, jenkinsFacade);

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
        columns.add(createFileColumn());
        columns.add(createAgeColumn());

        var authorsSize = new ColumnBuilder().withHeaderLabel(Messages.Table_Column_AuthorsSize())
                .withDataPropertyKey("authorsSize")
                .withResponsivePriority(1)
                .withType(ColumnType.NUMBER)
                .build();
        columns.add(authorsSize);
        var commitsSize = new ColumnBuilder().withHeaderLabel(Messages.Table_Column_CommitsSize())
                .withDataPropertyKey("commitsSize")
                .withResponsivePriority(1)
                .withType(ColumnType.NUMBER)
                .build();
        columns.add(commitsSize);
        var modifiedAt = new ColumnBuilder().withHeaderLabel(Messages.Table_Column_LastCommit())
                .withDataPropertyKey("modifiedAt")
                .withResponsivePriority(50)
                .withHeaderClass(ColumnCss.DATE)
                .build();
        columns.add(modifiedAt);
        var addedAt = new ColumnBuilder().withHeaderLabel(Messages.Table_Column_AddedAt())
                .withDataPropertyKey("addedAt")
                .withResponsivePriority(50)
                .withHeaderClass(ColumnCss.DATE)
                .build();
        columns.add(addedAt);
        var linesOfCode = new ColumnBuilder().withHeaderLabel(Messages.Table_Column_LOC())
                .withDataPropertyKey("linesOfCode")
                .withResponsivePriority(25)
                .withType(ColumnType.NUMBER)
                .build();
        columns.add(linesOfCode);
        var churn = new ColumnBuilder().withHeaderLabel(Messages.Table_Column_Churn())
                .withDataPropertyKey("churn")
                .withResponsivePriority(25)
                .withType(ColumnType.NUMBER)
                .build();
        columns.add(churn);
        columns.add(createHiddenDetailsColumn());

        return columns;
    }

    @Override
    public ForensicsRow getRow(final Issue issue) {
        var row = new ForensicsRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(),
                issue, getJenkinsFacade());
        if (statistics.contains(issue.getFileName())) {
            var result = statistics.get(issue.getFileName());
            row.setAuthorsSize(String.valueOf(result.getNumberOfAuthors()));
            row.setCommitsSize(String.valueOf(result.getNumberOfCommits()));
            row.setModifiedAt(result.getLastModificationTime());
            row.setAddedAt(result.getCreationTime());
            row.setLinesOfCode(result.getLinesOfCode());
            row.setChurn(result.getAbsoluteChurn());
        }
        else {
            row.setAuthorsSize(UNDEFINED);
            row.setCommitsSize(UNDEFINED);
            row.setModifiedAt(0);
            row.setAddedAt(0);
            row.setLinesOfCode(0);
            row.setChurn(0);
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
        private int linesOfCode;
        private int churn;

        ForensicsRow(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
                final DescriptionProvider descriptionProvider, final Issue issue, final JenkinsFacade jenkinsFacade) {
            super(ageBuilder, fileNameRenderer, descriptionProvider, issue, jenkinsFacade);
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

        public int getLinesOfCode() {
            return linesOfCode;
        }

        public void setLinesOfCode(final int linesOfCode) {
            this.linesOfCode = linesOfCode;
        }

        public int getChurn() {
            return churn;
        }

        public void setChurn(final int churn) {
            this.churn = churn;
        }
    }
}
