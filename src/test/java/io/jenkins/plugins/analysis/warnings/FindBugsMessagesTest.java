package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import io.jenkins.plugins.analysis.warnings.FindBugsMessages.Pattern;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

/**
 * Tests the class {@link FindBugsMessages}.
 */
class FindBugsMessagesTest {
    /** Bug ID for test. */
    private static final String NP_STORE_INTO_NONNULL_FIELD = "NP_STORE_INTO_NONNULL_FIELD";
    /** Expected number of patterns. */
    private static final int EXPECTED_PATTERNS = 468;
    /** Expected number of patterns in fb-contrib. */
    private static final int EXPECTED_CONTRIB_PATTERNS = 302;
    /** Expected number of patterns in find-sec-bugs. */
    private static final int EXPECTED_SECURITY_PATTERNS = 128;
    private static final String PATH_TRAVERSAL_IN = "PATH_TRAVERSAL_IN";

    @Test
    void shouldReadAllMessageFiles() {
        assertThat(createMessages().size()).isEqualTo(
                EXPECTED_PATTERNS + EXPECTED_CONTRIB_PATTERNS + EXPECTED_SECURITY_PATTERNS);
    }

    @Test
    void shouldReadAllFindBugsMessages() {
        assertThat(readMessages("messages.xml")).hasSize(EXPECTED_PATTERNS);
    }

    @Test
    void shouldReadAllFindSecBugsMessages() {
        assertThat(readMessages("find-sec-bugs-messages.xml")).hasSize(EXPECTED_SECURITY_PATTERNS);
    }

    @Test
    void shouldReadAllFbContribMessages() {
        assertThat(readMessages("fb-contrib-messages.xml")).hasSize(EXPECTED_CONTRIB_PATTERNS);
    }

    @Test
    void shouldMapMessagesToTypes() {
        FindBugsMessages messages = createMessages();
        String expectedMessage = "A value that could be null is stored into a field that has been annotated as @Nonnull.";
        assertThat(messages.getMessage(NP_STORE_INTO_NONNULL_FIELD, Locale.ENGLISH))
                .contains(expectedMessage);
        assertThat(messages.getMessage(NP_STORE_INTO_NONNULL_FIELD, Locale.GERMAN))
                .contains(expectedMessage); // there is no German translation

        assertThat(messages.getShortMessage(NP_STORE_INTO_NONNULL_FIELD, Locale.ENGLISH))
                .isEqualTo("Store of null value into field annotated @Nonnull");

        assertThat(messages.getMessage("NMCS_NEEDLESS_MEMBER_COLLECTION_SYNCHRONIZATION", Locale.ENGLISH))
                .contains("This class defines a private collection member as synchronized. It appears");
        assertThat(messages.getShortMessage("NMCS_NEEDLESS_MEMBER_COLLECTION_SYNCHRONIZATION", Locale.ENGLISH))
                .isEqualTo("Class defines unneeded synchronization on member collection");
    }

    @Test
    void shouldProvideLocalizedMessagesForFrench() {
        FindBugsMessages messages = createMessages();
        assertThat(messages.getShortMessage(NP_STORE_INTO_NONNULL_FIELD, Locale.FRANCE))
                .contains("Stocke une valeur null dans");
        assertThat(messages.getMessage(NP_STORE_INTO_NONNULL_FIELD, Locale.FRANCE))
                .contains("Une valeur qui pourrait");
    }

    @Test
    void issue55707() {
        FindBugsMessages messages = createMessages();
        assertThat(messages.getShortMessage(PATH_TRAVERSAL_IN, Locale.ENGLISH))
                .isEqualTo("Potential Path Traversal (file read)");
        assertThat(messages.getMessage(PATH_TRAVERSAL_IN, Locale.ENGLISH))
                .contains("A file is opened to read its content. The filename comes from an <b>input</b> parameter. ");
    }

    private List<Pattern> readMessages(final String fileName) {
        try (InputStream file = read(fileName)) {
            return new FindBugsMessages().parse(file);
        }
        catch (IOException | SAXException e) {
            throw new AssertionError(e);
        }
    }

    private FindBugsMessages createMessages() {
        FindBugsMessages messages = new FindBugsMessages();
        messages.initialize();
        return messages;
    }

    private InputStream read(final String fileName) {
        return FindBugsMessages.class.getResourceAsStream("findbugs-messages/" + fileName);
    }
}
