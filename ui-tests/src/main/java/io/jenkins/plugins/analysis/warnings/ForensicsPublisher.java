package io.jenkins.plugins.analysis.warnings;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * {@link PageObject} representing  the ForensicsPublisher of the forensics API plugin.
 *
 * @author Ullrich Hafner
 */
@Describable("Mine SCM repository")
public class ForensicsPublisher extends AbstractStep implements PostBuildStep {
    /**
     * Creates a new page object.
     *
     * @param parent
     *         parent page object
     * @param path
     *         path on the parent page
     */
    public ForensicsPublisher(final Job parent, final String path) {
        super(parent, path);
    }
}
