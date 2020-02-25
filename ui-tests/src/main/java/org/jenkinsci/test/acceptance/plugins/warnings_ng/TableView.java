package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * A simple Page object for the Table View view of the warnings plugin (white mountains release).
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
public class TableView extends AbstractStep {
    private final Control issues = control("#issuesContent");

    /**
     * Creates a new page object.
     *
     * @param parent
     *         parent page object
     * @param path
     *         path on the parent page
     */
    public TableView(final Job parent, final String path) {
        super(parent, path);
    }

    /**
     * Perform a simple click on the issues tab of the table view.
     */
    public void selectIssuesTab() {
        issues.find(by.href("#issuesContent")).click();
    }

}
