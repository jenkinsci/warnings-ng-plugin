package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page object for build informations about a specific tool.
 *
 * @author Florian Hageneder
 */
public class BuildInfoPage {
    private final List<String> infoMessages;
    private final List<String> errorMessages;

    /**
     * Parse information from the given page.
     *
     * @param page
     *         Page to gather information from.
     */
    public BuildInfoPage(final HtmlPage page) {
        infoMessages = StreamSupport.stream(page.getElementById("info")
                .getChildElements().spliterator(), false)
                .map(DomElement::getFirstChild)
                .map(DomNode::getNodeValue)
                .collect(Collectors.toList());

        errorMessages = StreamSupport.stream(page.getElementById("errors")
                .getChildElements().spliterator(), false)
                .map(DomElement::getFirstChild)
                .map(DomNode::getNodeValue)
                .collect(Collectors.toList());
    }

    public List<String> getInfoMessages() {
        return infoMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BuildInfoPage that = (BuildInfoPage) o;
        return Objects.equals(infoMessages, that.infoMessages) &&
                Objects.equals(errorMessages, that.errorMessages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(infoMessages, errorMessages);
    }

    @Override
    public String toString() {
        return "BuildInfoPage{" + "infoMessages=" + infoMessages + ", errorMessages=" + errorMessages + '}';
    }
}
