package io.jenkins.plugins.analysis.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueTest;
import edu.hm.hafner.analysis.Priority;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import net.sf.json.JSONArray;
import static org.assertj.core.api.Assertions.assertThat;

import hudson.util.XStream2;

/**
 * Tests the class {@link BuildIssue}.
 *
 * @author Ullrich Hafner
 */
class BuildIssueTest extends IssueTest {
    @Test
    void shouldBeConvertibleToJson() {
        Locale.setDefault(Locale.ENGLISH);

        IssueBuilder builder = new IssueBuilder();
        Issue issue = builder.setFileName("path/to/file-1")
                .setPackageName("package-1")
                .setCategory("category-1")
                .setType("type-1")
                .setLineStart(15)
                .setPriority(Priority.HIGH).build();

        BuildIssue buildIssue = new BuildIssue(issue, 1);

        JSONArray columns = JSONArray.fromObject(buildIssue.toJson());

        assertThatJson(columns).isArray().ofLength(5);
        assertThatColumnsAreValid(columns, 1);
    }

    static void assertThatColumnsAreValid(final JSONArray columns, int index) {
        String actual = columns.getString(0);
        assertThat(actual).matches(createFileLinkMatcher("file-" + index, 15));
        assertThat(columns.get(1)).isEqualTo(createPropertyLink("packageName", "package-" + index));
        assertThat(columns.get(2)).isEqualTo(createPropertyLink("category", "category-" + index));
        assertThat(columns.get(3)).isEqualTo(createPropertyLink("type", "type-" + index));
        assertThat(columns.get(4)).isEqualTo("<a href=\"HIGH\">High</a>");
    }

    private static String createPropertyLink(final String property, final String value) {
        return String.format("<a href=\"%s.%d/\">%s</a>", property, value.hashCode(), value);
    }

    private static String createFileLinkMatcher(final String fileName, final int lineNumber) {
        return "<a href=\"source.[0-9a-f-]+/#" + lineNumber + "\">"
                + fileName + ":" + lineNumber
                + "</a>";
    }

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