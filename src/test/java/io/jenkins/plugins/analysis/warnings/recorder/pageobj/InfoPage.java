package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class InfoPage {
    private final List<String> infoMessages;

    private final List<String> errorMessages;

    public InfoPage(final HtmlPage infoPage) {
        infoMessages = parseMessages(infoPage.getElementById("info"));
        errorMessages = parseMessages(infoPage.getElementById("errors"));
    }

    private List<String> parseMessages(final DomElement element) {
        List<String> messages = new ArrayList<>();
        if (Objects.nonNull(element)) {
            element.getChildElements().forEach(m -> messages.add(m.asText()));
        }
        return messages;
    }

    public List<String> getInfoMessages() {
        return infoMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
