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

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 4, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                7,
                "LEAK 150 direct bytes 0x005a2540-0x005a25d6 + 0 indirect bytes<br>" +
                        "# 0 replace_malloc                                     [/drmemory_package/common/alloc_replace.c:2576]<br>" +
                        "# 1 test_open_address_hashmap_initialize               [/open_address_hash/test_open_address_hash.c:84]<br>" +
                        "# 2 main                                               [/open_address_hash/run_open_address_hash.c:7]",
                "/open_address_hash/run_open_address_hash.c",
                TYPE, "Leak", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                7,
                "POSSIBLE LEAK 150 direct bytes 0x005a25f8-0x005a268e + 0 indirect bytes<br>" +
                        "# 0 replace_malloc                                              [/drmemory_package/common/alloc_replace.c:2576]<br>" +
                        "# 1 test_open_address_hashmap_compute_simple_hash               [/open_address_hash/test_open_address_hash.c:107]<br>" +
                        "# 2 main                                                        [/open_address_hash/run_open_address_hash.c:7]",
                "/open_address_hash/run_open_address_hash.c",
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
        annotation = iterator.next();
        checkWarning(annotation,
                16,
                "UNINITIALIZED READ: reading 0xb741b004-0xb741b012 14 byte(s) within 0xb741b000-0xb741b012<br>" +
                        "# 0 system call write parameter #1<br>" +
                        "# 1 libc.so.6!__GI___libc_write                    [../sysdeps/unix/syscall-template.S:81]<br>" +
                        "# 2 libc.so.6!_IO_new_file_write                   [/build/eglibc-X4bnBz/eglibc-2.19/libio/fileops.c:1261]<br>" +
                        "# 3 libc.so.6!new_do_write                         [/build/eglibc-X4bnBz/eglibc-2.19/libio/fileops.c:538]<br>" +
                        "# 4 libc.so.6!_IO_new_do_write                     [/build/eglibc-X4bnBz/eglibc-2.19/libio/fileops.c:511]<br>" +
                        "# 5 libc.so.6!__GI__IO_file_sync                   [/build/eglibc-X4bnBz/eglibc-2.19/libio/fileops.c:892]<br>" +
                        "# 6 libc.so.6!__GI__IO_fflush                      [/build/eglibc-X4bnBz/eglibc-2.19/libio/iofflush.c:41]<br>" +
                        "# 7 fll_write_node                                 [/var/lib/jenkins/jobs/jobs_name/workspace/jenkins_config/iondb/src/tests/unit/dictionary/linear_hash/run_linear_hash.c:16]<br>" +
                        "# 8 fll_create                                     [/var/lib/jenkins/jobs/jobs_name/workspace/jenkins_config/iondb/src/tests/unit/dictionary/linear_hash/run_linear_hash.c:16]<br>" +
                        "# 9 create_test_linked_list                        [/var/lib/jenkins/jobs/jobs_name/workspace/jenkins_config/iondb/src/tests/unit/dictionary/linear_hash/test_file_linked_list.c:24]<br>" +
                        "#10 test_file_linked_list_initialize               [/var/lib/jenkins/jobs/jobs_name/workspace/jenkins_config/iondb/src/tests/unit/dictionary/linear_hash/test_file_linked_list.c:55]<br>" +
                        "#11 planck_unit_run_suite                          [/var/lib/jenkins/jobs/jobs_name/workspace/jenkins_config/iondb/src/tests/unit/dictionary/linear_hash/run_linear_hash.c:16]<br>" +
                        "#12 runalltests_file_linked_list                   [/var/lib/jenkins/jobs/jobs_name/workspace/jenkins_config/iondb/src/tests/unit/dictionary/linear_hash/test_file_linked_list.c:776]<br>" +
                        "#13 main                                           [/var/lib/jenkins/jobs/jobs_name/workspace/jenkins_config/iondb/src/tests/unit/dictionary/linear_hash/run_linear_hash.c:11]",
                "/var/lib/jenkins/jobs/jobs_name/workspace/jenkins_config/iondb/src/tests/unit/dictionary/linear_hash/run_linear_hash.c",
                TYPE, "Unitialized Read", Priority.HIGH);
    }

    @Override
    protected String getWarningsFile() {
        return "drmemory.txt";
    }
}

