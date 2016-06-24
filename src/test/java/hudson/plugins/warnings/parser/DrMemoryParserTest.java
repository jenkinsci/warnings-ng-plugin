package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Tests the class {@link DrMemoryParser}.
 */
public class DrMemoryParserTest extends ParserTester {
    private static final String TYPE = new DrMemoryParser().getGroup();

    /**
     * Parses a file with two Dr. Memory warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new DrMemoryParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                84,
                "LEAK 150 direct bytes 0x005a2540-0x005a25d6 + 0 indirect bytes<br>" +
                        "# 0 replace_malloc                                     [/drmemory_package/common/alloc_replace.c:2576]<br>" +
                        "# 1 test_open_address_hashmap_initialize               [/open_address_hash/test_open_address_hash.c:84]<br>" +
                        "# 2 main                                               [/open_address_hash/run_open_address_hash.c:7]",
                "/open_address_hash/test_open_address_hash.c",
                TYPE, "Leak", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                107,
                "POSSIBLE LEAK 150 direct bytes 0x005a25f8-0x005a268e + 0 indirect bytes<br>" +
                        "# 0 replace_malloc                                              [/drmemory_package/common/alloc_replace.c:2576]<br>" +
                        "# 1 test_open_address_hashmap_compute_simple_hash               [/open_address_hash/test_open_address_hash.c:107]<br>" +
                        "# 2 main                                                        [/open_address_hash/run_open_address_hash.c:7]",
                "/open_address_hash/test_open_address_hash.c",
                TYPE, "Possible Leak", Priority.NORMAL);
        annotation = iterator.next();
        checkWarning(annotation,
                0,
                "LEAK 150 direct bytes 0x005a25f8-0x005a268e + 0 indirect bytes<br>" +
                        "# 0 replace_malloc<br>" +
                        "# 1 test_open_address_hashmap_compute_complex_hash<br>" +
                        "# 2 main",
                "Unknown",
                TYPE, "Leak", Priority.HIGH);
    }

    @Override
    protected String getWarningsFile() {
        return "drmemory.txt";
    }
}

