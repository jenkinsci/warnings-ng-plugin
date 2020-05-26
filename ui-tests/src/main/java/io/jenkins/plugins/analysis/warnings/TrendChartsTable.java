package io.jenkins.plugins.analysis.warnings;

import java.net.URL;

import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.PageObject;

public class TrendChartsTable extends PageObject {
    private String jobName;

    public TrendChartsTable(final Injector injector, final URL url) {
        super(injector, url);
    }

    protected TrendChartsTable(final PageObject context, final URL url) {
        super(context, url);
    }

    public TrendChartsTable(final Build parent, final String jobName) {
        super(parent, parent.url(""));

        this.jobName = jobName;
    }
}
