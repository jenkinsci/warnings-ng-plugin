package io.jenkins.plugins.analysis.warnings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.RevapiInfoExtension;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.DescriptionProvider;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
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
            return new CompatibilityModel(report, getFileNameRenderer(build), getAgeBuilder(build, url), this,
                    new JenkinsFacade());
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
            CompatibilityRow row = new CompatibilityRow(getAgeBuilder(), getFileNameRenderer(), getDescriptionProvider(), issue, getJenkinsFacade());
            final Serializable additionalInfos = issue.getAdditionalProperties();
            if (additionalInfos instanceof RevapiInfoExtension) {
                final RevapiInfoExtension revapiInfo = (RevapiInfoExtension) additionalInfos;
                row.setOldFile(revapiInfo.getOldFile());
                row.setNewFile(revapiInfo.getNewFile());
                row.setIssueName(revapiInfo.getIssueName());
                row.setSeverities(revapiInfo.getSeverities());
            }
            row.setSeverity(issue);
            return row;
        }

        @Override
        public String getId() {
            return "issues";
        }

        @Override
        public List<TableColumn> getColumns() {
            List<TableColumn> columns = new ArrayList<>();
            columns.add(new TableColumn("Name", "issueName").setWidth(2));

            columns.add(new TableColumn("old file", "oldFile").setWidth(2));
            columns.add(new TableColumn("new file", "newFile").setWidth(2));
            columns.add(createFileColumn());
            columns.add(new TableColumn("binary", "binary"));
            columns.add(new TableColumn("source", "source"));

            //columns.add(new TableColumn("severity", "severity"));
            columns.add(createSeverityColumn().setWidth(2));
            columns.add(createDetailsColumn());

            columns.add(createAgeColumn());
            columns.add(createHiddenDetailsColumn());
            return columns;
        }

        @SuppressWarnings("PMD.DataClass")
        public static class CompatibilityRow extends TableRow {
            private Map<String, String> severities = new HashMap<>();
            private String issueName;
            private String oldFile;
            private String newFile;
            private String severity;

            protected CompatibilityRow(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
                    final DescriptionProvider descriptionProvider, final Issue issue,
                    final JenkinsFacade jenkinsFacade) {
                super(ageBuilder, fileNameRenderer, descriptionProvider, issue, jenkinsFacade);
            }

            public String getBinary() {
                return severities.get("BINARY");
            }

            public String getSource() {
                return severities.get("SOURCE");
            }

            public String getIssueName() {
                return issueName;
            }

            public String getOldFile() {
                return oldFile;
            }

            public String getNewFile() {
                return newFile;
            }

            public String getSeverity(){
                return severity;
            }

            public void setIssueName(final String issueName) {
                this.issueName = issueName;
            }

            public void setOldFile(final String oldFile) {
                this.oldFile = oldFile;
            }

            public void setNewFile(final String newFile) {
                this.newFile = newFile;
            }

            public void setSeverities(final Map<String,String> severities){
                this.severities = severities;
            }

            void setSeverity(final Issue issue) {
                severity = formatSeverity(issue.getSeverity());
            }
        }
    }
}

