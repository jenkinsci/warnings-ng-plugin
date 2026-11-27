package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.util.JenkinsFacade;
import java.util.List;

/**
 * Provides the dynamic model for the details table that shows the fixed issues of a build, i.e., the issues of the
 * reference build that are not reported in the current build anymore. The table uses the columns of the
 * {@link IssuesModel}, except for the age: the age of an issue that does not exist anymore is meaningless.
 *
 * <p>
 * The affected files of fixed issues are part of the reference build, so the file name renderer of this model
 * should check the build folder of the reference build.
 * </p>
 *
 * @author Ullrich Hafner
 */
class FixedIssuesModel extends IssuesModel {
    FixedIssuesModel(
            final Report report,
            final FileNameRenderer fileNameRenderer,
            final AgeBuilder ageBuilder,
            final DescriptionProvider descriptionProvider) {
        this(report, fileNameRenderer, ageBuilder, descriptionProvider, new JenkinsFacade());
    }

    @VisibleForTesting
    FixedIssuesModel(
            final Report report,
            final FileNameRenderer fileNameRenderer,
            final AgeBuilder ageBuilder,
            final DescriptionProvider descriptionProvider,
            final JenkinsFacade jenkinsFacade) {
        super(report, fileNameRenderer, ageBuilder, descriptionProvider, jenkinsFacade);
    }

    @Override
    public String getId() {
        return "fixed";
    }

    @Override
    public List<TableColumn> getColumns() {
        var ageLabel = createAgeColumn().getHeaderLabel();
        return super.getColumns().stream()
                .filter(column -> !ageLabel.equals(column.getHeaderLabel()))
                .toList();
    }
}
