package io.jenkins.plugins.analysis.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueTest;
import edu.hm.hafner.analysis.Priority;
import static edu.hm.hafner.analysis.assertj.Assertions.*;

import hudson.util.XStream2;

/**
 * Tests the class {@link BuildIssue}.
 *
 * @author Ullrich Hafner
 */
class BuildIssueTest extends IssueTest {
    @SuppressWarnings("ParameterNumber")
    @Override
    protected Issue createIssue(final String fileName, final int lineStart, final int lineEnd, final int columnStart,
            final int columnEnd, final String category, final String type, final String packageName,
            final String moduleName, final Priority priority, final String message, final String description,
            final String origin, final String fingerprint) {
        Issue issue = super.createIssue(fileName, lineStart, lineEnd, columnStart, columnEnd, category, type,
                packageName, moduleName, priority, message, description, origin, fingerprint);
        return new BuildIssue(issue, 1);
    }

    /** Verifies that an build issue has the same ID as the wrapped issue. */
    @Test
    void shouldUseIdOfWrappedElement() {
        IssueBuilder builder = new IssueBuilder();
        Issue emptyIssue = builder.build();
        int build = 1;
        BuildIssue issue = new BuildIssue(emptyIssue, build);

        assertThat(issue.getBuild()).isEqualTo(build);
        assertThat(issue.getId()).isEqualTo(emptyIssue.getId());
    }

    /** Ensures that an issue instance can be serialized and deserialized using XStream. */
    @Test
    void shouldBeSerializableWithXStream() throws IOException {
        XStream2 stream = BuildIssue.createStream();

        byte[] bytes = asBytes(createFilledIssue(), stream);

        assertThatIssueCanBeRestoredFrom(bytes, stream);
    }

    /**
     * Verifies that saved XStream XML serialized format (from a previous release) still can be resolved with the
     * current implementation of {@link BuildIssue}.
     */
    @Test
    void shouldReadIssueFromOldXmlSerialization() {
        byte[] restored = readResource("issue.xml");

        XStream2 stream = BuildIssue.createStream();

        assertThatIssueCanBeRestoredFrom(restored, stream);
    }

    private void assertThatIssueCanBeRestoredFrom(final byte[] bytes, final XStream2 stream) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            Object issue = stream.fromXML(inputStream);

            assertThat(issue).isInstanceOf(BuildIssue.class);
            assertThat(issue).isEqualTo(createFilledIssue());
        }
        catch (IOException e) {
            throw new AssertionError("Can' resolve BuildIssue from byte array", e);
        }
    }

    private byte[] asBytes(final Issue issue, final XStream2 stream) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            stream.toXMLUTF8(issue, out);
            return out.toByteArray();
        }
    }

    /**
     * Serializes an issues to a file. Use this method in case the issue properties have been changed and the
     * readResolve method has been adapted accordingly so that the old serialization still can be read.
     *
     * @param args
     *         not used
     *
     * @throws IOException
     *         if the file could not be written
     */
    public static void useIfSerializationChanges(final String... args) throws IOException {
        new BuildIssueTest().createSerializationFile();
    }
}