package io.jenkins.plugins.analysis.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.LineRange;
import edu.hm.hafner.analysis.LineRangeList;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.ResourceTest;

import hudson.util.XStream2;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link ReportXmlStream}.
 *
 * @author Ullrich Hafner
 */
public class ReportXmlStreamITest extends ResourceTest {
    private static final int LINE_START = 1;
    private static final int LINE_END = 2;
    private static final int COLUMN_START = 3;
    private static final int COLUMN_END = 4;
    private static final String CATEGORY = "category";
    private static final String TYPE = "type";
    private static final String PACKAGE_NAME = "package-name";
    private static final String FILE_NAME = "file-name";
    private static final String MODULE_NAME = "module-name";
    private static final Severity PRIORITY = Severity.WARNING_HIGH;
    private static final String MESSAGE = "message";
    private static final String DESCRIPTION = "description";
    private static final String FINGERPRINT = "fingerprint";
    private static final String ORIGIN = "origin";
    private static final String REFERENCE = "reference";
    private static final LineRangeList LINE_RANGES = new LineRangeList(singletonList(new LineRange(5, 6)));

    /** Required to enable Jenkins security settings during serialization. */
    @ClassRule
    public static final JenkinsRule JENKINS = new JenkinsRule();

    /**
     * Ensures that a {@link Report} can be serialized and deserialized using XStream.
     *
     * @throws IOException
     *         if the file could not be written
     */
    @Test @SuppressWarnings("PMD.SystemPrintln")
    public void shouldSerializeReportWithXStream() throws IOException {
        XStream2 stream = new ReportXmlStream().createStream();

        byte[] bytes = asBytes(createReport(), stream);

        System.out.print(new String(bytes));

        assertThatReportCanBeRestoredFrom(stream, bytes);
    }

    private void assertThatReportCanBeRestoredFrom(final XStream2 stream, final byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            Object issue = stream.fromXML(inputStream);

            assertThat(issue).isInstanceOf(Report.class);

            String xml = new String(asBytes(createReport(), new ReportXmlStream().createStream()));

            assertThat(issue).as(xml).isEqualTo(createReport());
        }
        catch (IOException e) {
            throw new AssertionError("Can' resolve Report from byte array", e);
        }
    }

    private Report createReport() {
        Report report = new Report().addAll(createFilledIssue("1"), createFilledIssue("2"));
        report.logError("error");
        report.logInfo("info");
        return report;
    }

    /**
     * Ensures that an {@link Issue} can be serialized and deserialized using XStream.
     *
     * @throws IOException
     *         if the file could not be written
     */
    @Test @SuppressWarnings("PMD.SystemPrintln")
    public void shouldSerializeIssueWithXStream() throws IOException {
        XStream2 stream = new ReportXmlStream().createStream();

        byte[] bytes = asBytes(createFilledIssue(MESSAGE), stream);
        System.out.print(new String(bytes));

        assertThatIssueCanBeRestoredFrom(bytes, stream);
    }

    private Issue createFilledIssue(final String message) {
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
                .setSeverity(PRIORITY)
                .setMessage(message)
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
    public void shouldReadIssueFromOldXmlSerialization() {
        XStream2 stream = new ReportXmlStream().createStream();

        byte[] restored = readAllBytes("issue.xml");

        assertThatIssueCanBeRestoredFrom(restored, stream);
    }

    private void assertThatIssueCanBeRestoredFrom(final byte[] bytes, final XStream2 stream) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            Object issue = stream.fromXML(inputStream);

            assertThat(issue).isInstanceOf(Issue.class);

            String xml = new String(asBytes(createFilledIssue(MESSAGE), new ReportXmlStream().createStream()));

            assertThat(issue).as(xml).isEqualTo(createFilledIssue(MESSAGE));
        }
        catch (IOException e) {
            throw new AssertionError("Can' resolve Issue from byte array", e);
        }
    }

    private byte[] asBytes(final Object issue, final XStream2 stream) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            stream.toXMLUTF8(issue, out);
            return out.toByteArray();
        }
    }
}
