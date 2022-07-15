package io.jenkins.plugins.analysis.warnings;

import java.util.List;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.DescriptionProvider;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner.DryModel;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.util.JenkinsFacade;

public class DifferentCompatibilityScanner extends AnalysisModelParser {
    private static final long serialVersionUID = 5105550534032476746L;



    public static class CompatibilityLabelProvider extends StaticAnalysisLabelProvider {

        public CompatibilityLabelProvider(final String id, final String name) {
            super(id, name);
        }

        @Override
        public DetailsTableModel getIssuesModel(final Run<?, ?> build, final String url,
                final Report report) {
            return new CompatibilityModel(report, getFileNameRenderer(build), getAgeBuilder(build, url), this, new JenkinsFacade());
        }
    }

    public static class CompatibilityModel extends DetailsTableModel {

        /**
         * Creates a new instance of {@link DetailsTableModel}.
         *
         * @param report
         *         the report to render
         * @param fileNameRenderer
         *         renders the file name column
         * @param ageBuilder
         *         renders the age column
         * @param descriptionProvider
         *         renders the description text
         * @param jenkinsFacade
         *         Jenkins facade to replaced with a stub during unit tests
         */
        protected CompatibilityModel(final Report report,
                final FileNameRenderer fileNameRenderer,
                final AgeBuilder ageBuilder,
                final DescriptionProvider descriptionProvider,
                final JenkinsFacade jenkinsFacade) {
            super(report, fileNameRenderer, ageBuilder, descriptionProvider, jenkinsFacade);
        }

        @Override
        protected TableRow getRow(final Issue issue) {
            return null;
        }

        @Override
        public String getId() {
            return "issues";
        }

        @Override
        public List<TableColumn> getColumns() {
            return null;
        }
    }
}
