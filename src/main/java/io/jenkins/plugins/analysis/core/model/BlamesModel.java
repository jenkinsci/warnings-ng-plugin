package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.datatables.api.TableColumn;
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

    private final Blames blames;

    BlamesModel(final Report report, final Blames blames, final FileNameRenderer fileNameRenderer,
            final AgeBuilder ageBuilder, final DescriptionProvider labelProvider) {
        super(report, fileNameRenderer, ageBuilder, labelProvider);

        this.blames = blames;
    }

    @Override
    public String getId() {
        return "blames";
    }

    @Override
    public List<TableColumn> getColumns() {
        List<TableColumn> columns = new ArrayList<>();

        columns.add(createDetailsColumn());
        columns.add(createFileColumn());
        columns.add(createAgeColumn());
        columns.add(new TableColumn(Messages.Table_Column_Author(), "author"));
        columns.add(new TableColumn(Messages.Table_Column_Email(), "email"));
        columns.add(new TableColumn(Messages.Table_Column_Commit(), "commit"));

        return columns;
    }

    @Override
    protected BlamesRow getRow(final Issue issue) {
        BlamesRow row = new BlamesRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(), issue);
        if (blames.contains(issue.getFileName())) {
            FileBlame blameRequest = blames.getBlame(issue.getFileName());
            int line = issue.getLineStart();
            row.setAuthor(blameRequest.getName(line));
            row.setEmail(blameRequest.getEmail(line));
            row.setCommit(blameRequest.getCommit(line));
        }
        else {
            row.setAuthor(UNDEFINED);
            row.setEmail(UNDEFINED);
            row.setCommit(UNDEFINED);
        }
        return row;
    }

    /**
     * A table row that shows the source control blames.
     */
    @SuppressWarnings("PMD.DataClass") // Used to automatically convert to JSON object
    public static class BlamesRow extends TableRow {
        private String author;
        private String email;
        private String commit;

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

        void setAuthor(final String author) {
            this.author = author;
        }

        void setEmail(final String email) {
            this.email = email;
        }

        void setCommit(final String commit) {
            this.commit = commit;
        }
    }
}
