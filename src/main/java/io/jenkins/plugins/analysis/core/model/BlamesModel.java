package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
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
 *     This blames model consists of the following columns:
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

    BlamesModel(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
            final DescriptionProvider descriptionProvider, final Blames blames) {
        super(ageBuilder, fileNameRenderer, descriptionProvider);

        this.blames = blames;
    }

    @Override
    public List<Integer> getWidths(final Report report) {
        List<Integer> widths = new ArrayList<>();
        widths.add(1);
        widths.add(1);
        widths.add(1);
        widths.add(1);
        widths.add(1);
        widths.add(1);
        return widths;
    }

    @Override
    public List<String> getHeaders(final Report report) {
        List<String> visibleColumns = new ArrayList<>();
        visibleColumns.add(Messages.Table_Column_Details());
        visibleColumns.add(Messages.Table_Column_File());
        visibleColumns.add(Messages.Table_Column_Age());
        visibleColumns.add(Messages.Table_Column_Author());
        visibleColumns.add(Messages.Table_Column_Email());
        visibleColumns.add(Messages.Table_Column_Commit());
        return visibleColumns;
    }

    @Override
    public BlamesRow getRow(final Report report, final Issue issue, final String description) {
        BlamesRow row = new BlamesRow();
        row.setDescription(formatDetails(issue, description));
        row.setFileName(formatFileName(issue));
        row.setAge(formatAge(issue));
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

    @Override
    public String getColumnsDefinition(final Report report) {
        StringBuilder builder = new StringBuilder("[");
        builder.append("{\"data\": \"description\"},");
        builder.append("{\"data\": \"fileName\"},");
        builder.append("{\"data\": \"age\"},");
        builder.append("{\"data\": \"author\"},");
        builder.append("{\"data\": \"email\"},");
        builder.append("{\"data\": \"commit\"}");
        builder.append("]");
        return builder.toString();
    }

    public static class BlamesRow {
        private String description;
        private String fileName;
        private String age;
        private String author;
        private String email;
        private String commit;

        public String getDescription() {
            return description;
        }

        public String getFileName() {
            return fileName;
        }

        public String getAge() {
            return age;
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

        void setDescription(final String description) {
            this.description = description;
        }

        void setFileName(final String fileName) {
            this.fileName = fileName;
        }

        void setAge(final String age) {
            this.age = age;
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
