package io.jenkins.plugins.analysis.core.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
    public ForensicsRow getRow(final Report report, final Issue issue, final String description) {
        ForensicsRow row = new ForensicsRow();
        row.setDescription(formatDetails(issue, description));
        row.setFileName(formatFileName(issue));
        row.setAge(formatAge(issue));
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
    public String getColumnsDefinition(final Report report) {
        return "["
                + "{\"data\": \"description\"},"
                + "{\"data\": \"fileName\"},"
                + "{\"data\": \"age\"},"
                + "{\"data\": \"authorsSize\"},"
                + "{\"data\": \"commitsSize\"},"
                + "{"
                + "    \"type\": \"num\","
                + "    \"data\": \"addedDays\","
                + "    \"render\": {"
                + "        \"_\": \"display\","
                + "        \"sort\": \"days\""
                + "    }"
                + "},"
                + "{"
                + "    \"type\": \"num\","
                + "    \"data\": \"addedDays\","
                + "    \"render\": {"
                + "        \"_\": \"display\","
                + "        \"sort\": \"days\""
                + "    }"
                + "}"
                + "]";
    }

    public static class ForensicsRow {
        private String description;
        private String fileName;
        private String age;
        private String authorsSize;
        private String commitsSize;
        private PrettyElapsedTime modifiedDays;
        private PrettyElapsedTime addedDays;

        public String getDescription() {
            return description;
        }

        public String getFileName() {
            return fileName;
        }

        public String getAge() {
            return age;
        }

        public String getAuthorsSize() {
            return authorsSize;
        }

        public String getCommitsSize() {
            return commitsSize;
        }

        public PrettyElapsedTime getModifiedDays() {
            return modifiedDays;
        }

        public PrettyElapsedTime getAddedDays() {
            return addedDays;
        }

        void setDescription(final String description) {
            this.description = description;
        }

        void setFileName(final String fileName) {
            this.fileName = fileName;
        }

        void setAge(final String age) {
            this.age = age;
        }

        void setAuthorsSize(final String authorsSize) {
            this.authorsSize = authorsSize;
        }

        void setCommitsSize(final String commitsSize) {
            this.commitsSize = commitsSize;
        }

        void setModifiedDays(final long modifiedDays) {
            this.modifiedDays = new PrettyElapsedTime();
            this.modifiedDays.setDays(modifiedDays);
            this.modifiedDays.setDisplay(getElapsedTime(modifiedDays));
        }

        void setAddedDays(final long addedDays) {
            this.addedDays = new PrettyElapsedTime();
            this.addedDays.setDays(addedDays);
            this.addedDays.setDisplay(getElapsedTime(addedDays));
        }

        private String getElapsedTime(final long days) {
            PrettyTime prettyTime = new PrettyTime();
            return prettyTime.format(
                    Date.from(LocalDate.now().minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
    }

    public static class PrettyElapsedTime {
        private String display;
        private long days;

        public String getDisplay() {
            return display;
        }

        void setDisplay(final String display) {
            this.display = display;
        }

        public long getDays() {
            return days;
        }

        void setDays(final long days) {
            this.days = days;
        }
    }
}
