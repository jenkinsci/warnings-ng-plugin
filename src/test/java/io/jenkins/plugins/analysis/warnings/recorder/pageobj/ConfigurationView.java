package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlNumberInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ConfigurationView {

    private static final String HEALTHY_ID = "_.healthy";
    private static final String UNHEALTHY_ID = "_.unhealthy";
    private HtmlForm form;
    public ConfigurationView(final HtmlPage configPage) {
        form = configPage.getFormByName("config");
    }

    public void submit() throws IOException {
        HtmlFormUtil.submit(form);
    }


    public void setHealthy(final int healthy) {
        HtmlNumberInput textField = form.getInputByName(HEALTHY_ID);
        textField.setText(String.valueOf(healthy));
    }

    public void setUnhealthy(final int unhealthy) {
        HtmlNumberInput textField = form.getInputByName(UNHEALTHY_ID);
        textField.setText(String.valueOf(unhealthy));
    }

}
