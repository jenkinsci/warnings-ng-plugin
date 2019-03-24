package io.jenkins.plugins.analysis.core.scm;

import java.io.Serializable;

import edu.hm.hafner.analysis.Report;

public class GitWorker implements Serializable {

    public GitResults process(final Report filtered) {
        return new GitResults();
    }
}
