package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests the class {@link GnatParser}.
 */
public class GnatParserTest extends ParserTester {
    /**
     * Creates a new instance of {@link GnatParserTest}.
     */
    public GnatParserTest() {
        super(GnatParser.class);
    }

    /**
     * Parses a file with 9 Gnat warnings.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new GnatParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 9, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();

        // /home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/utilities/class_utilities.adb:402:23:
        // warning: call to obsolescent procedure "Very_Verbose" declared at
        // debug.ads:59
        checkWarning(
                annotation,
                402,
                "call to obsolescent procedure \"Very_Verbose\" declared at debug.ads:59",
                "/home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/utilities/class_utilities.adb",
                GnatParser.WARNING_TYPE, "GNAT warning", Priority.NORMAL);

        // /home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/utilities/iml-interfaces-cfg.adb:63:14:
        // warning: variable "E" is not referenced
        annotation = iterator.next();
        checkWarning(
                annotation,
                63,
                "variable \"E\" is not referenced",
                "/home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/utilities/iml-interfaces-cfg.adb",
                GnatParser.WARNING_TYPE, "GNAT warning", Priority.NORMAL);

        // /home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/pointsto/andersen_results.adb:96:80:
        // (style) this line is too long
        annotation = iterator.next();
        checkWarning(
                annotation,
                96,
                "this line is too long",
                "/home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/pointsto/andersen_results.adb",
                GnatParser.WARNING_TYPE, "GNAT style", Priority.LOW);

        // /home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/generated/ada_delta_constraints.adb:3:06:
        // warning: redundant with clause in body
        annotation = iterator.next();
        checkWarning(
                annotation,
                3,
                "redundant with clause in body",
                "/home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/generated/ada_delta_constraints.adb",
                GnatParser.WARNING_TYPE, "GNAT warning", Priority.NORMAL);

        // /home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/reuse/src/array_tables.adb:97:07:
        // warning: variable "Dummy_Empty_Array_Item" is read but never assigned
        annotation = iterator.next();
        checkWarning(
                annotation,
                97,
                "variable \"Dummy_Empty_Array_Item\" is read but never assigned",
                "/home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/reuse/src/array_tables.adb",
                GnatParser.WARNING_TYPE, "GNAT warning", Priority.NORMAL);

        // /home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/reuse/src/graph_algorithms-generic_explorers.adb:63:14:
        // warning: "C" is not modified, could be declared constant
        annotation = iterator.next();
        checkWarning(
                annotation,
                63,
                "\"C\" is not modified, could be declared constant",
                "/home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/reuse/src/graph_algorithms-generic_explorers.adb",
                GnatParser.WARNING_TYPE, "GNAT warning", Priority.NORMAL);

        // /home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/reuse/src/process_data.adb:257:49:
        // (style) bad casing of "False" declared in Standard
        annotation = iterator.next();
        checkWarning(
                annotation,
                257,
                "bad casing of \"False\" declared in Standard",
                "/home/bergerbd/.hudson/jobs/Test/workspace/projects/libs/reuse/src/process_data.adb",
                GnatParser.WARNING_TYPE, "GNAT style", Priority.LOW);

        // /home/bergerbd/.hudson/jobs/Test/workspace/projects/tools/scangen/src/scangen.adb:23:35:
        // error: binary operator expected
        annotation = iterator.next();
        checkWarning(
                annotation,
                23,
                "binary operator expected",
                "/home/bergerbd/.hudson/jobs/Test/workspace/projects/tools/scangen/src/scangen.adb",
                GnatParser.WARNING_TYPE, "GNAT error", Priority.HIGH);

        // /home/bergerbd/.hudson/jobs/Test/workspace/projects/tools/scangen/src/scangen.adb:23:36:
        // error: identifier cannot start with underline
        annotation = iterator.next();
        checkWarning(
                annotation,
                23,
                "identifier cannot start with underline",
                "/home/bergerbd/.hudson/jobs/Test/workspace/projects/tools/scangen/src/scangen.adb",
                GnatParser.WARNING_TYPE, "GNAT error", Priority.HIGH);
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "gnat.txt";
    }
}

