package hudson.plugins.analysis.core;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Test;

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
     * Verifies that a simple file without path is correctly mapped.
     *
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testSimpleFileNameMapping() throws Exception {
        String[] workspaceFiles = {SCANNED_FILENAME};
        ParserResult result = createParserResult(workspaceFiles);

        FileAnnotation warning = mockWarning("file.txt");
        result.addAnnotation(warning);

        verify(warning).setFileName(FOUND_FILE_NAME);
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

        FileAnnotation warning = mockWarning("to/file.txt");
        result.addAnnotation(warning);

        verify(warning).setFileName(FOUND_FILE_NAME);
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

        FileAnnotation warning = mockWarning("to/file.txt");
        result.addAnnotation(warning);

        verify(warning).setFileName(FOUND_FILE_NAME);
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

        FileAnnotation warning = mockWarning("path/to/file.txt");
        result.addAnnotation(warning);

        verify(warning).setFileName(FOUND_FILE_NAME);
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

