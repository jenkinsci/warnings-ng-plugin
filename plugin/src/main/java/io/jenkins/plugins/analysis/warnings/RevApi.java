package io.jenkins.plugins.analysis.warnings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.RevApiInfoExtension;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.DescriptionProvider;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableColumn.ColumnBuilder;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Provides a parser and customized messages for RevApi. Delegates to {@link }
 */
public class RevApi extends AnalysisModelParser {
    private static final long serialVersionUID = -8571635906342563283L;
    private static final String ID = "revapi";

    /** Creates a new instance of {@link RevApi}. */
    @DataBoundConstructor
    public RevApi() {
        super();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("revApi")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new RevApi.RevApiLabelProvider(getId(), getDisplayName());
        }
    }

    /**
     * Label for revApi Issues.
     */
    protected static class RevApiLabelProvider extends StaticAnalysisLabelProvider {

        public RevApiLabelProvider(final String id, final String name) {
            super(id, name);
        }

        @Override
        public DetailsTableModel getIssuesModel(final Run<?, ?> build, final String url, final Report report) {
            return new RevApiModel(report, getFileNameRenderer(build), getAgeBuilder(build, url), this,
                    new JenkinsFacade());
        }
    }

    /**
     * Custom RevApiModel to show different columns.
     */
    protected static class RevApiModel extends DetailsTableModel {

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
        public RevApiModel(final Report report,
                final FileNameRenderer fileNameRenderer,
                final AgeBuilder ageBuilder,
                final DescriptionProvider descriptionProvider,
                final JenkinsFacade jenkinsFacade) {
            super(report, fileNameRenderer, ageBuilder, descriptionProvider, jenkinsFacade);
        }

        @Override
        protected TableRow getRow(final Issue issue) {
            return new RevApiRow(getAgeBuilder(), getFileNameRenderer(),
                    getDescriptionProvider(), issue, getJenkinsFacade(), issue.getAdditionalProperties());
        }

        @Override
        public String getId() {
            return "issues";
        }

        @Override
        public List<TableColumn> getColumns() {
            List<TableColumn> columns = new ArrayList<>();
            columns.add(createDetailsColumn());
            TableColumn nameColumn = new ColumnBuilder().withHeaderLabel(Messages.RevApi_Table_Column_Name())
                    .withDataPropertyKey("issueName")
                    .withResponsivePriority(100)
                    .build();
            columns.add(nameColumn);

            TableColumn oldFileColumn = new ColumnBuilder().withHeaderLabel(Messages.RevApi_Table_Column_oldFile())
                    .withDataPropertyKey("oldFile")
                    .withResponsivePriority(50)
                    .build();
            columns.add(oldFileColumn);

            TableColumn newFileColumn = new ColumnBuilder().withHeaderLabel(Messages.RevApi_Table_Column_newFile())
                    .withDataPropertyKey("newFile")
                    .withResponsivePriority(50)
                    .build();
            columns.add(newFileColumn);

            TableColumn categoryColumn = new ColumnBuilder().withHeaderLabel(Messages.RevApi_Table_Column_category())
                    .withDataPropertyKey("category")
                    .withResponsivePriority(50)
                    .build();
            columns.add(categoryColumn);

            TableColumn binaryColumn = new ColumnBuilder().withHeaderLabel(Messages.RevApi_Table_Column_binary())
                    .withDataPropertyKey("binary")
                    .withResponsivePriority(30)
                    .build();
            columns.add(binaryColumn);

            TableColumn sourceColumn = new ColumnBuilder().withHeaderLabel(Messages.RevApi_Table_Column_source())
                    .withDataPropertyKey("source")
                    .withResponsivePriority(30)
                    .build();
            columns.add(sourceColumn);

            columns.add(createSeverityColumn());
            columns.add(createAgeColumn());
            return columns;
        }


        /**
         * Custom RevApi Issue rows.
         */
        @SuppressWarnings("PMD.DataClass")
        protected static class RevApiRow extends TableRow {
            private Map<String, String> severities = new HashMap<>();
            private String issueName;
            private String oldFile;
            private String newFile;
            private String severity;
            private String category;

            protected RevApiRow(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
                    final DescriptionProvider descriptionProvider, final Issue issue,
                    final JenkinsFacade jenkinsFacade, final Serializable additionalData) {
                super(ageBuilder, fileNameRenderer, descriptionProvider, issue, jenkinsFacade);
                if (additionalData instanceof RevApiInfoExtension) {
                    final RevApiInfoExtension revApiInfo = (RevApiInfoExtension) additionalData;
                    setOldFile(revApiInfo.getOldFile());
                    setNewFile(revApiInfo.getNewFile());
                    setIssueName(revApiInfo.getIssueName());
                    setSeverities(revApiInfo.getSeverities());
                    setCategory(issue.getCategory());
                    setSeverity(issue);
                }
                else {
                    throw new IllegalStateException();
                }
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

            public String getSeverity() {
                return severity;
            }

            public String getCategory() {
                return category;
            }

            public void setIssueName(final String issueName) {
                this.issueName = issueName;
            }

            public void setOldFile(final String oldFile) {
                if (oldFile.equals("null")) {
                    this.oldFile = "-";
                }
                else {
                    this.oldFile = oldFile;
                }
            }

            public void setNewFile(final String newFile) {
                if (newFile.equals("null")) {
                    this.newFile = "-";
                }
                else {
                    this.newFile = newFile;
                }
            }

            public void setSeverities(final Map<String, String> severities) {
                this.severities = severities;
            }

            public void setCategory(final String category) {
                this.category = category;
            }

            void setSeverity(final Issue issue) {
                severity = formatSeverity(issue.getSeverity());
            }
        }
    }
}
