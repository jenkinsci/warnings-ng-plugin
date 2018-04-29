package io.jenkins.plugins.analysis.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.LineRange;
import edu.hm.hafner.analysis.LineRangeList;
import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.util.ResourceTest;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import hudson.util.XStream2;

/**
 * Tests the class {@link IssueStream}.
 *
 * @author Ullrich Hafner
 */
class IssueStreamTest extends ResourceTest {
    private static final int LINE_START = 1;
    private static final int LINE_END = 2;
    private static final int COLUMN_START = 3;
    private static final int COLUMN_END = 4;
    private static final String CATEGORY = "category";
    private static final String TYPE = "type";
    private static final String PACKAGE_NAME = "package-name";
    private static final String FILE_NAME = "file-name";
    private static final String MODULE_NAME = "module-name";
    private static final Priority PRIORITY = Priority.HIGH;
    private static final String MESSAGE = "message";
    private static final String DESCRIPTION = "description";
    private static final String FINGERPRINT = "fingerprint";
    private static final String ORIGIN = "origin";
    private static final String REFERENCE = "reference";
    private static final LineRangeList LINE_RANGES = new LineRangeList(singletonList(new LineRange(5, 6)));

    /** Ensures that an issue instance can be serialized and deserialized using XStream. */
    @Test
    void shouldBeSerializableWithXStream() throws IOException {
        XStream2 stream = new IssueStream().createStream();

        byte[] bytes = asBytes(createFilledIssue(), stream);
        System.out.printf(new String(bytes));

        assertThatIssueCanBeRestoredFrom(bytes, stream);
    }

    private Issue createFilledIssue() {
        IssueBuilder builder = new IssueBuilder();
        builder.setFileName(FILE_NAME)
                .setLineStart(LINE_START)
                .setLineEnd(LINE_END)
                .setColumnStart(COLUMN_START)
                .setColumnEnd(COLUMN_END)
                .setCategory(CATEGORY)
                .setType(TYPE)
                .setPackageName(PACKAGE_NAME)
                .setModuleName(MODULE_NAME)
                .setPriority(PRIORITY)
                .setMessage(MESSAGE)
                .setDescription(DESCRIPTION)
                .setOrigin(ORIGIN)
                .setLineRanges(LINE_RANGES)
                .setFingerprint(FINGERPRINT)
                .setReference(REFERENCE);
        return builder.build();
    }

    /**
     * Verifies that saved XStream XML serialized format (from a previous release) still can be resolved with the
     * current implementation of {@link Issue}.
     */
    @Test
    void shouldReadIssueFromOldXmlSerialization() {
        IssueStream model = new IssueStream();
        XStream2 stream = model.createStream();

        byte[] restored = readAllBytes("issue.xml");

        assertThatIssueCanBeRestoredFrom(restored, stream);
    }

    private void assertThatIssueCanBeRestoredFrom(final byte[] bytes, final XStream2 stream) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            Object issue = stream.fromXML(inputStream);

            assertThat(issue).isInstanceOf(Issue.class);

            String xml = new String(asBytes(createFilledIssue(), new IssueStream().createStream()));

            assertThat(issue).as(xml).isEqualTo(createFilledIssue());
        }
        catch (IOException e) {
            throw new AssertionError("Can' resolve Issue from byte array", e);
        }
    }

    private byte[] asBytes(final Issue issue, final XStream2 stream) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            stream.toXMLUTF8(issue, out);
            return out.toByteArray();
        }
    }
}