package io.jenkins.plugins.analysis.core.scm;

import java.io.Serializable;
import java.util.logging.Logger;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import hudson.scm.SCM;

public class GsWorker implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(GsWorker.class.getName());

    private SCM scm;

    public GsWorker(final SCM scm) {
        this.scm = scm;
        //GitClient gitClient;
        //try {
        //    gitClient = (GitClient) scm;
        //} catch (Exception)
    }

    public GsResults process(final Report filteredReport) {
        filteredReport.logInfo("Process GsResults with '%s' results", filteredReport.getSize());

        for (Issue issue : filteredReport) {
            filteredReport.logInfo("issue: '%s'", issue.toString());
        }
        return new GsResults();
    }
}
