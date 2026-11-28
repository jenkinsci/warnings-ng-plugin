package io.jenkins.plugins.analysis.core.steps;

import hudson.console.HyperlinkNote;
import hudson.model.Run;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates the console version of the message that announces a new result: the ID of the result is rendered as a
 * hyperlink to the result page. The plain text of the message is not modified, so that the messages stored in the
 * result (and shown on the Log Messages page) do not contain any console markup.
 *
 * @author Amit Kumar
 */
class AttachMessageLinker {
    /** The format of the message that is logged when a result is attached to a build. */
    static final String ATTACHING_MESSAGE = "Attaching ResultAction with ID '%s' to build '%s'.";

    private final Run<?, ?> run;
    private final String id;

    AttachMessageLinker(final Run<?, ?> run, final String id) {
        this.run = run;
        this.id = id;
    }

    /**
     * Returns a copy of the specified messages, where the ID in the "Attaching ResultAction" message is a hyperlink to
     * the result page.
     *
     * @param infoMessages
     *         the info messages of the report
     *
     * @return the messages that should be printed to the console
     */
    List<String> linkResult(final Iterable<String> infoMessages) {
        var plain = ATTACHING_MESSAGE.formatted(id, run);
        var url = "/" + run.getUrl() + id;
        var linked = ATTACHING_MESSAGE.formatted(HyperlinkNote.encodeTo(url, id), run);

        List<String> messages = new ArrayList<>();
        for (String message : infoMessages) {
            messages.add(plain.equals(message) ? linked : message);
        }
        return messages;
    }
}
