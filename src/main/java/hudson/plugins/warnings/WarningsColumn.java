package hudson.plugins.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

import hudson.plugins.analysis.views.WarningsCountColumn;

import hudson.views.ListViewColumnDescriptor;

/**
 * A column that shows the total number of compiler warnings in a job.
 *
 * @author Ulli Hafner
 */
public class WarningsColumn extends WarningsCountColumn<AggregatedWarningsProjectAction> {
    /**
     * Creates a new instance of {@link WarningsColumn}.
     */
    @DataBoundConstructor
    public WarningsColumn() { // NOPMD: data binding
        super();
    }

    @Override
    protected Class<AggregatedWarningsProjectAction> getProjectAction() {
        return AggregatedWarningsProjectAction.class;
    }

    @Override
    public String getColumnCaption() {
        return Messages.Warnings_Warnings_ColumnHeader();
    }

    /**
     * Descriptor for the column.
     */
    @Extension
    public static class ColumnDescriptor extends ListViewColumnDescriptor {
        @Override
        public boolean shownByDefault() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return Messages.Warnings_Warnings_Column();
        }
    }
}
