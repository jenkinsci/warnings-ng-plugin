package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.assertj.core.util.Lists;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page-object-pattern wrapper for a {@link io.jenkins.plugins.analysis.warnings.Java} job build information page.
 */
public class JavaInfoPage {

    /**
     * Relative Path for the lastBuild information page.
     */
    public static final String PATH = "lastBuild/java/info";
    private HtmlPage infoPage;

    /**
     * Create a new page object for {@link io.jenkins.plugins.analysis.warnings.Java} build information.
     *
     * @param page
     *         the {@link HtmlPage} to wrap
     */
    public JavaInfoPage(final HtmlPage page) {
        this.infoPage = page;
    }

    public List<String> getErrorMessages() {
        return getMessages("error");
    }

    public List<String> getInfoMessages() {
        return getMessages("info");
    }

    private List<String> getMessages(final String id) {
        DomElement domElement = infoPage.getElementById(id);

        if (domElement == null) {
            return Lists.emptyList();
        }
        
        return StreamSupport.stream(domElement.getChildElements().spliterator(), false)
                .map(DomNode::getTextContent)
                .collect(Collectors.toList());
    }
}
