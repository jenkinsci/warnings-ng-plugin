package hudson.plugins.analysis.core;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import hudson.plugins.analysis.core.ParserResult.Workspace;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link ParserResult}.
 *
 * @author Ulli Hafner
 */
public class ParserResultTest {
    private static final String OTHER_SCANNED_FILE = "other/file.txt";
    private static final String SCANNED_FILENAME = "relative/path/to/file.txt";
    private static final String SCANNED_FILENAME_WINDOWS = "relative\\path\\to\\file.txt";
    private static final String WORSPACE_ROOT = "ws";
    private static final String FOUND_FILE_NAME = WORSPACE_ROOT + "/" + SCANNED_FILENAME;

    /**
     * Verifies that the relative prefix of a path is stripped.
     */
    @Test
    public void shouldStripRelativePathPrefix() {
        ParserResult parserResult = new ParserResult();

        verifyPrefix(parserResult, "file.txt");
        verifyPrefix(parserResult, "../file.txt");
        verifyPrefix(parserResult, "../../file.txt");
        verifyPrefix(parserResult, "../../../file.txt");

        verifyPrefix(parserResult, "./../file.txt");

        verifyPrefix(parserResult, "../bla/../file.txt");

        assertEquals("Wrong prefix removal: ", "fi/file.txt", parserResult.stripRelativePrefix("fi/file.txt"));
    }

    private void verifyPrefix(final ParserResult parserResult, final String fileName) {
        assertEquals("Wrong prefix removal: ", "file.txt", parserResult.stripRelativePrefix(fileName));
    }

    /**
     * Verifies that the workspace scanning of files works with relative paths that contain references
     * to parent directories.
     *
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-32150">Issue 32150</a>
     */
    @Test
    public void issue32150() throws Exception {
        String[] workspaceFiles = {"directory-a/multi-file-in-subdir.txt",
                "directory-b/multi-file-in-subdir.txt",
                "directory-b/subdir/file2",
                "file.txt",
                "directory-a/subdir/file1",
                "directory-a/file-in-subdir.txt",
                "compile-log.txt"};
        ParserResult result = createParserResult(workspaceFiles);

        verifyWarningPath(result, "file.txt", "file.txt");
        verifyWarningPath(result, "file-in-subdir.txt", "directory-a/file-in-subdir.txt");
        verifyNoWarningPath(result, "multi-file-in-subdir.txt");

        verifyWarningPath(result, "../file.txt", "file.txt");
        verifyWarningPath(result, "../file-in-subdir.txt", "directory-a/file-in-subdir.txt");
        verifyNoWarningPath(result, "../multi-file-in-subdir.txt");

        verifyWarningPath(result, "../directory-a/../directory-a/../directory-a/file-in-subdir.txt", "directory-a/file-in-subdir.txt");
        verifyWarningPath(result, "directory-a/../directory-a/../directory-a/file-in-subdir.txt", "directory-a/file-in-subdir.txt");
    }

    private void verifyNoWarningPath(final ParserResult result, final String fileName) {
        FileAnnotation warning = mockWarning(fileName);
        result.addAnnotation(warning);
        verify(warning, never()).setFileName(anyString());
    }

    private void verifyWarningPath(final ParserResult result, final String fileName, final String workspacePath) {
        verifyWarning(result, fileName, WORSPACE_ROOT + "/" + workspacePath);
    }

    private void verifyWarning(final ParserResult result, final String fileName, final String foundFileName) {
        FileAnnotation warning = mockWarning(fileName);
        result.addAnnotation(warning);
        verify(warning).setFileName(foundFileName);
    }

    /**
     * Verifies that the number of annotations is correctly returned.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testCountingOfAddAnnotation() {
        ParserResult result = new ParserResult();
        FileAnnotation warning = mockWarning("file.txt");
        assertEquals("Warning correctly added.", 1, result.addAnnotation(warning));
        assertEquals("Warning correctly not added.", 0, result.addAnnotation(warning));
    }

    /**
     * Verifies that the number of annotations is correctly returned.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testCountingOfAddAnnotations() {
        ParserResult result = new ParserResult();
        List<FileAnnotation> warnings = Lists.newArrayList();
        warnings.add(mockWarning("1"));
        warnings.add(mockWarning("2"));
        assertEquals("Warnings correctly added.", 2, result.addAnnotations(warnings));
        assertEquals("Warnings correctly not added.", 0, result.addAnnotations(warnings));
    }

    /**
     * Verifies that a simple file without path is correctly mapped.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testSimpleFileNameMapping() throws Exception {
        String[] workspaceFiles = {SCANNED_FILENAME};
        ParserResult result = createParserResult(workspaceFiles);

        verifyWarning(result, "file.txt", FOUND_FILE_NAME);
    }

    /**
     * Verifies that a file with path prefix is correctly mapped.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testFileNameMappingWithPrefix() throws Exception {
        String[] workspaceFiles = {SCANNED_FILENAME};
        ParserResult result = createParserResult(workspaceFiles);

        verifyWarning(result, "to/file.txt", FOUND_FILE_NAME);
    }

    /**
     * Verifies that a file (in Windows format) with path prefix is correctly mapped.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testWindowsFileNameMappingWithPrefix() throws Exception {
        String[] workspaceFiles = {SCANNED_FILENAME_WINDOWS};
        ParserResult result = createParserResult(workspaceFiles);

        verifyWarning(result, "to/file.txt", FOUND_FILE_NAME);
    }

    /**
     * Verifies that no file is mapped if the workspace contains duplicates and
     * the warning could not be assigned.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testDuplicate() throws Exception {
        String[] workspaceFiles = {SCANNED_FILENAME, OTHER_SCANNED_FILE};
        ParserResult result = createParserResult(workspaceFiles);

        FileAnnotation warning = mockWarning("file.txt");
        result.addAnnotation(warning);

        verify(warning, never()).setFileName(anyString());
    }

    private ParserResult createParserResult(String[] workspaceFiles) throws IOException, InterruptedException {
        return new ParserResult(mockWorkspace(workspaceFiles), true);
    }

    /**
     * Verifies that the file is mapped if the workspace contains duplicates and
     * the warning could be assigned because the prefix is unique.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testUniquePrefixDuplicate() throws Exception {
        ParserResult result = createParserResult(new String[] {SCANNED_FILENAME, OTHER_SCANNED_FILE});

        verifyWarning(result, "path/to/file.txt", FOUND_FILE_NAME);
    }

    private FileAnnotation mockWarning(final String fileName) {
        FileAnnotation warning = mock(FileAnnotation.class);
        when(warning.getFileName()).thenReturn(fileName);
        when(warning.getPriority()).thenReturn(Priority.HIGH);
        return warning;
    }

    private Workspace mockWorkspace(final String[] workspaceFiles) throws IOException, InterruptedException {
        Workspace workspace = mock(Workspace.class);
        when(workspace.child(anyString())).thenReturn(workspace);
        when(workspace.getPath()).thenReturn(WORSPACE_ROOT);
        when(workspace.findFiles(anyString())).thenReturn(workspaceFiles);
        return workspace;
    }
}

