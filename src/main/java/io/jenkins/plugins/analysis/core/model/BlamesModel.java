package io.jenkins.plugins.analysis.core.model;

import java.util.Arrays;
import java.util.List;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileBlame;

/**
 * Provides the dynamic model for the details table that shows the source control blames.
 *
 * <p>
 * This blames model consists of the following columns:
 * </p>
 * <ul>
 * <li>issue details (message and description)</li>
 * <li>file name</li>
 * <li>age</li>
 * <li>SCM blame author name</li>
 * <li>SCM blame author email</li>
 * <li>SCM blame commit ID</li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
public class BlamesModel extends DetailsTableModel {
    static final String UNDEFINED = "-";
    static final int UNDEFINED_DATE = 0;

    private final Blames blames;

    BlamesModel(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
            final DescriptionProvider descriptionProvider, final Blames blames) {
        super(ageBuilder, fileNameRenderer, descriptionProvider);

        this.blames = blames;
    }

    @Override
    public List<String> getHeaders(final Report report) {
        return Arrays.asList(
                Messages.Table_Column_Details(),
                Messages.Table_Column_File(),
                Messages.Table_Column_Age(),
                Messages.Table_Column_Author(),
                Messages.Table_Column_Email(),
                Messages.Table_Column_Commit(),
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
                DATE_HEADER_CLASS);
    }

    @Override
    public List<Integer> getWidths(final Report report) {
        return Arrays.asList(1, 1, 1, 1, 1, 1, 1);
    }

    @Override
    public BlamesRow getRow(final Report report, final Issue issue) {
        BlamesRow row = new BlamesRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(), issue);
        if (blames.contains(issue.getFileName())) {
            FileBlame blameRequest = blames.getBlame(issue.getFileName());
            int line = issue.getLineStart();
            row.setAuthor(blameRequest.getName(line));
            row.setEmail(blameRequest.getEmail(line));
            row.setCommit(blameRequest.getCommit(line));
            row.setAddedAt(blameRequest.getTime(line));
        }
        else {
            row.setAuthor(UNDEFINED);
            row.setEmail(UNDEFINED);
            row.setCommit(UNDEFINED);
            row.setAddedAt(UNDEFINED_DATE);
        }
        return row;
    }

    @Override
    public void configureColumns(final ColumnDefinitionBuilder builder, final Report report) {
        builder.add("description")
                .add("fileName", "string")
                .add("age")
                .add("author")
                .add("email")
                .add("commit")
                .add("addedAt");
    }

    /**
     * A table row that shows the source control blames.
     */
    @SuppressWarnings("PMD.DataClass") // Used to automatically convert to JSON object
    public static class BlamesRow extends TableRow {
        private String author;
        private String email;
        private String commit;
        private int addedAt;

        BlamesRow(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
                final DescriptionProvider descriptionProvider, final Issue issue) {
            super(ageBuilder, fileNameRenderer, descriptionProvider, issue);
        }

        public String getAuthor() {
            return author;
        }

        public String getEmail() {
            return email;
        }

        public String getCommit() {
            return commit;
        }

        public int getAddedAt() {
            return addedAt;
        }

        void setAuthor(final String author) {
            this.author = author;
        }

        void setEmail(final String email) {
            this.email = email;
        }

        void setCommit(final String commit) {
            this.commit = commit;
        }

        void setAddedAt(final int addedAt) {
            this.addedAt = addedAt;
        }
    }
}
