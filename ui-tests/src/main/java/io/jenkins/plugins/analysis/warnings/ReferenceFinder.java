package io.jenkins.plugins.analysis.warnings;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * Page object for the {@code SimpleReferenceRecorder} of the forensics API plugin.
 *
 * @author Ullrich Hafner
 */
@Describable("Discover reference build")
public class ReferenceFinder extends AbstractStep implements PostBuildStep {
    /**
     * Creates a new page object.
     *
     * @param parent
     *         parent page object
     * @param path
     *         path on the parent page
     */
    public ReferenceFinder(final Job parent, final String path) {
        super(parent, path);
    }
}
