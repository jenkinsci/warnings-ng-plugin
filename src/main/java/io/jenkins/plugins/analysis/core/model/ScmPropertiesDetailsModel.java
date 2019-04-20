package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.scm.GsResult;
import io.jenkins.plugins.analysis.core.scm.GsResults;

/**
 * Provides the model for the source control details table. The model consists of the following parts:
 *
 * <ul>
 * <li>header name for each column</li>
 * <li>width for each column</li>
 * <li>content for each row</li>
 * <li>content for whole table</li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
class ScmPropertiesDetailsModel extends DetailsTableModel {
    static final String UNDEFINED = "-";

    private final GsResults gsResults;

    ScmPropertiesDetailsModel(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
            final DescriptionProvider descriptionProvider, final GsResults gsResults) {
        super(ageBuilder, fileNameRenderer, descriptionProvider);

        this.gsResults = gsResults;
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
        // FIXME: add correct headers
        visibleColumns.add("#Committers");
        visibleColumns.add("#Changes");
        visibleColumns.add("#LastModified");
        return visibleColumns;
    }

    @Override
    protected List<String> getRow(final Report report, final Issue issue, final String description) {
        List<String> columns = new ArrayList<>();
        columns.add(formatDetails(issue, description));
        columns.add(formatFileName(issue));
        columns.add(formatAge(issue));
        if (gsResults.contains(issue.getFileName())) {
            GsResult result = gsResults.get(issue.getFileName());
            // FIXME: add correct formatting
            columns.add(String.valueOf(result.getSize()));
            columns.add(UNDEFINED);
            columns.add(String.valueOf(result.getAge()));
        }
        else {
            columns.add(UNDEFINED);
            columns.add(UNDEFINED);
            columns.add(UNDEFINED);
        }
        return columns;
    }
}
