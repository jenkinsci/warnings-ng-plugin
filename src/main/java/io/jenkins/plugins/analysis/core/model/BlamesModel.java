package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.util.Blame;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableColumn.ColumnCss;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.util.JenkinsFacade;

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

    BlamesModel(final Report report, final Blames blames, final FileNameRenderer fileNameRenderer,
            final AgeBuilder ageBuilder, final DescriptionProvider labelProvider) {
        this(report, blames, fileNameRenderer, ageBuilder, labelProvider, new JenkinsFacade());
    }

    @VisibleForTesting
    BlamesModel(final Report report, final Blames blames, final FileNameRenderer fileNameRenderer,
            final AgeBuilder ageBuilder, final DescriptionProvider labelProvider, final JenkinsFacade jenkinsFacade) {
        super(report, fileNameRenderer, ageBuilder, labelProvider, jenkinsFacade);

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
        columns.add(new TableColumn(Messages.Table_Column_AddedAt(), "addedAt")
                .setHeaderClass(ColumnCss.DATE));

        return columns;
    }

    @Override
    protected BlamesRow getRow(final Issue issue) {
        return new BlamesRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(),
                issue, getJenkinsFacade(), new Blame(issue, blames));
    }

    /**
     * A table row that shows the source control blames.
     */
    @SuppressWarnings("PMD.DataClass") // Used to automatically convert to JSON object
    public static class BlamesRow extends TableRow {
        private final Blame blame;

        BlamesRow(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
                final DescriptionProvider descriptionProvider, final Issue issue, final JenkinsFacade jenkinsFacade,
                final Blame blame) {
            super(ageBuilder, fileNameRenderer, descriptionProvider, issue, jenkinsFacade);

            this.blame = blame;
        }

        public String getAuthor() {
            return blame.getAuthor();
        }

        public String getEmail() {
            return blame.getEmail();
        }

        public String getCommit() {
            return blame.getCommit();
        }

        public int getAddedAt() {
            return blame.getAddedAt();
        }
    }
}
