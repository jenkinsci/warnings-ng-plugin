package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.util.Blame;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableColumn.ColumnBuilder;
import io.jenkins.plugins.datatables.TableColumn.ColumnCss;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.util.CommitDecorator;
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

    private final Blames blames;
    private final CommitDecorator commitDecorator;

    BlamesModel(final Report report, final Blames blames, final FileNameRenderer fileNameRenderer,
            final AgeBuilder ageBuilder, final DescriptionProvider labelProvider,
            final CommitDecorator commitDecorator) {
        this(report, blames, fileNameRenderer, ageBuilder, labelProvider, commitDecorator, new JenkinsFacade());
    }

    @VisibleForTesting
    BlamesModel(final Report report, final Blames blames, final FileNameRenderer fileNameRenderer,
            final AgeBuilder ageBuilder, final DescriptionProvider labelProvider, final CommitDecorator commitDecorator,
            final JenkinsFacade jenkinsFacade) {
        super(report, fileNameRenderer, ageBuilder, labelProvider, jenkinsFacade);

        this.blames = blames;
        this.commitDecorator = commitDecorator;
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
        var author = new ColumnBuilder().withHeaderLabel(Messages.Table_Column_Author())
                .withDataPropertyKey("author")
                .withResponsivePriority(1)
                .build();
        columns.add(author);
        var email = new ColumnBuilder().withHeaderLabel(Messages.Table_Column_Email())
                .withDataPropertyKey("email")
                .withResponsivePriority(50)
                .build();
        columns.add(email);
        var commit = new ColumnBuilder().withHeaderLabel(Messages.Table_Column_Commit())
                .withDataPropertyKey("commit")
                .withResponsivePriority(10)
                .build();
        columns.add(commit);
        var addedAt = new ColumnBuilder().withHeaderLabel(Messages.Table_Column_AddedAt())
                .withDataPropertyKey("addedAt")
                .withResponsivePriority(25)
                .withHeaderClass(ColumnCss.DATE)
                .build();
        columns.add(addedAt);
        columns.add(createHiddenDetailsColumn());

        return columns;
    }

    @Override
    protected BlamesRow getRow(final Issue issue) {
        var blame = new Blame(issue, blames);
        return new BlamesRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(),
                issue, getJenkinsFacade(), blame, commitDecorator.asLink(blame.getCommit()));
    }

    /**
     * A table row that shows the source control blames.
     */
    @SuppressWarnings("PMD.DataClass") // Used to automatically convert to JSON object
    public static class BlamesRow extends TableRow {
        private final Blame blame;
        private final String commit;

        BlamesRow(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
                final DescriptionProvider descriptionProvider, final Issue issue, final JenkinsFacade jenkinsFacade,
                final Blame blame, final String commit) {
            super(ageBuilder, fileNameRenderer, descriptionProvider, issue, jenkinsFacade);

            this.blame = blame;
            this.commit = commit;
        }

        public String getAuthor() {
            return blame.getAuthorName();
        }

        public String getEmail() {
            return blame.getAuthorEmail();
        }

        public String getCommit() {
            return commit;
        }

        public int getAddedAt() {
            return blame.getAddedAt();
        }
    }
}
