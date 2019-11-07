package io.jenkins.plugins.analysis.core.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.ocpsoft.prettytime.PrettyTime;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
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

    ForensicsModel(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
            final DescriptionProvider descriptionProvider, final RepositoryStatistics statistics) {
        super(ageBuilder, fileNameRenderer, descriptionProvider);

        this.statistics = statistics;
    }

    @Override
    public List<String> getHeaders(final Report report) {
        return Arrays.asList(
                Messages.Table_Column_Details(),
                Messages.Table_Column_File(),
                Messages.Table_Column_Age(),
                Messages.Table_Column_AuthorsSize(),
                Messages.Table_Column_CommitsSize(),
                Messages.Table_Column_LastCommit(),
                Messages.Table_Column_AddedAt());
    }

    @Override
    public List<String> getHeaderClasses(final Report report) {
        return Arrays.asList(
                ANY_HEADER_CLASS,
                ANY_HEADER_CLASS,
                ANY_HEADER_CLASS,
                ANY_HEADER_CLASS,
                ANY_HEADER_CLASS,
                ANY_HEADER_CLASS,
                ANY_HEADER_CLASS);
    }

    @Override
    public List<Integer> getWidths(final Report report) {
        return Arrays.asList(1, 1, 1, 1, 1, 2, 2);
    }

    @Override
    public ForensicsRow getRow(final Report report, final Issue issue) {
        ForensicsRow row = new ForensicsRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(),
                issue);
        if (statistics.contains(issue.getFileName())) {
            FileStatistics result = statistics.get(issue.getFileName());
            row.setAuthorsSize(String.valueOf(result.getNumberOfAuthors()));
            row.setCommitsSize(String.valueOf(result.getNumberOfCommits()));
            row.setModifiedDays(result.getLastModifiedInDays());
            row.setAddedDays(result.getAgeInDays());
        }
        else {
            row.setAuthorsSize(UNDEFINED);
            row.setCommitsSize(UNDEFINED);
            row.setModifiedDays(0);
            row.setAddedDays(0);
        }
        return row;
    }

    @Override
    public void configureColumns(final ColumnDefinitionBuilder builder, final Report report) {
        builder.add("description").add("fileName", "string").add("age").add("authorsSize").add("commitsSize")
                .add("modifiedDays", "num")
                .add("addedDays", "num");
    }

    /**
     * A table row that shows the source control statistics.
     */
    @SuppressWarnings("PMD.DataClass") // Used to automatically convert to JSON object
    public static class ForensicsRow extends TableRow {
        private String authorsSize;
        private String commitsSize;
        private DetailedColumnDefinition modifiedDays;
        private DetailedColumnDefinition addedDays;

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

        public DetailedColumnDefinition getModifiedDays() {
            return modifiedDays;
        }

        public DetailedColumnDefinition getAddedDays() {
            return addedDays;
        }

        void setAuthorsSize(final String authorsSize) {
            this.authorsSize = authorsSize;
        }

        void setCommitsSize(final String commitsSize) {
            this.commitsSize = commitsSize;
        }

        void setModifiedDays(final long modifiedDays) {
            this.modifiedDays = new DetailedColumnDefinition(getElapsedTime(modifiedDays),
                    String.valueOf(modifiedDays));
        }

        void setAddedDays(final long addedDays) {
            this.addedDays = new DetailedColumnDefinition(getElapsedTime(addedDays), String.valueOf(addedDays));
        }

        private String getElapsedTime(final long days) {
            PrettyTime prettyTime = new PrettyTime();
            return prettyTime.format(
                    Date.from(LocalDate.now().minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
    }
}
