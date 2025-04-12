package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.RevApiInfoExtension;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * Provides a parser and customized messages for Revapi.
 */
public class RevApi extends AnalysisModelParser {
    @Serial
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
            // empty constructor required for stapler
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new RevApiLabelProvider(getId(), getDisplayName());
        }
    }

    private static class RevApiLabelProvider extends StaticAnalysisLabelProvider {
        RevApiLabelProvider(final String id, final String name) {
            super(id, name);
        }

        @Override
        public DetailsTableModel getIssuesModel(final Run<?, ?> build, final String url, final Report report) {
            return new RevApiModel(report, getFileNameRenderer(build), getAgeBuilder(build, url), this,
                    new JenkinsFacade());
        }
    }

    /**
     * Provides a customized table for Revapi issues.
     */
    public static class RevApiModel extends DetailsTableModel {
        RevApiModel(final Report report,
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
            var nameColumn = new ColumnBuilder().withHeaderLabel(Messages.RevApi_Table_Column_Name())
                    .withDataPropertyKey("issueName")
                    .withResponsivePriority(100)
                    .build();
            columns.add(nameColumn);

            var oldFileColumn = new ColumnBuilder().withHeaderLabel(Messages.RevApi_Table_Column_oldFile())
                    .withDataPropertyKey("oldFile")
                    .withResponsivePriority(50)
                    .build();
            columns.add(oldFileColumn);

            var newFileColumn = new ColumnBuilder().withHeaderLabel(Messages.RevApi_Table_Column_newFile())
                    .withDataPropertyKey("newFile")
                    .withResponsivePriority(50)
                    .build();
            columns.add(newFileColumn);

            var categoryColumn = new ColumnBuilder().withHeaderLabel(Messages.RevApi_Table_Column_category())
                    .withDataPropertyKey("category")
                    .withResponsivePriority(50)
                    .build();
            columns.add(categoryColumn);

            var binaryColumn = new ColumnBuilder().withHeaderLabel(Messages.RevApi_Table_Column_binary())
                    .withDataPropertyKey("binary")
                    .withResponsivePriority(30)
                    .build();
            columns.add(binaryColumn);

            var sourceColumn = new ColumnBuilder().withHeaderLabel(Messages.RevApi_Table_Column_source())
                    .withDataPropertyKey("source")
                    .withResponsivePriority(30)
                    .build();
            columns.add(sourceColumn);

            columns.add(createSeverityColumn());
            columns.add(createAgeColumn());
            return columns;
        }

        /**
         * A table row that shows the properties of a Revapi issue.
         */
        @SuppressWarnings("PMD.DataClass")
        public static class RevApiRow extends TableRow {
            private Map<String, String> severities;
            private final String issueName;
            private final String oldFile;
            private final String newFile;
            private final String severity;
            private String category;

            RevApiRow(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
                    final DescriptionProvider descriptionProvider, final Issue issue,
                    final JenkinsFacade jenkinsFacade, final Serializable additionalData) {
                super(ageBuilder, fileNameRenderer, descriptionProvider, issue, jenkinsFacade);
                if (additionalData instanceof RevApiInfoExtension revApiInfo) {
                    this.oldFile = revApiInfo.getOldFile();
                    this.newFile = revApiInfo.getNewFile();
                    this.issueName = revApiInfo.getIssueName();
                    this.severities = revApiInfo.getSeverities();
                    this.category = issue.getCategory();
                    this.severity = formatSeverity(issue.getSeverity());
                }
                else {
                    throw new IllegalStateException("Additional info of revApi Issue not an instance of RevApiInfoExtension");
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

            public void setSeverities(final Map<String, String> severities) {
                this.severities = severities;
            }

            public void setCategory(final String category) {
                this.category = category;
            }
        }
    }
}
