package io.jenkins.plugins.analysis.core.util;

import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import j2html.tags.DomContent;

import hudson.markup.MarkupFormatter;
import hudson.markup.RawHtmlMarkupFormatter;

/**
 * Sanitizes a piece of unsafe HTML code so that it can be rendered in an UI view.
 *
 * @author Ullrich Hafner
 */
public class Sanitizer {
    /** Sanitizes HTML elements in warning messages and tooltips. Use this formatter if raw HTML should be shown. */
    private final MarkupFormatter formatter = new RawHtmlMarkupFormatter(true);

    /**
     * Renders the specified HTML code. Removes unsafe HTML constructs.
     *
     * @param html
     *         the HTML to render
     *
     * @return safe HTML
     */
    public String render(final String html) {
        try {
            return formatter.translate(html);
        }
        catch (IOException e) {
            return ExceptionUtils.getRootCauseMessage(e);
        }
    }

    /**
     * Renders the specified HTML code. Removes unsafe HTML constructs.
     *
     * @param text
     *         the text to render
     *
     * @return safe HTML
     */
    public String render(final DomContent text) {
        return render(text.render());
    }
}
