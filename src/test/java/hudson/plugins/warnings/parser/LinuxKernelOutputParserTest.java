package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Tests the class {@link LinuxKernelOutputParser}.
 */
public class LinuxKernelOutputParserTest extends ParserTester {
    private static final String TYPE = new LinuxKernelOutputParser().getGroup();

    /**
     * Parse a kernel log file.
     *
     * @author Benedikt Spranger
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void testWarningsParser() throws IOException {
        Collection<FileAnnotation> warnings = new LinuxKernelOutputParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 26, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();

        checkWarning(annotation,
                     0,
                     "ACPI: RSDP 0x00000000000F68D0 000014 (v00 BOCHS )",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "ACPI: RSDT 0x0000000007FE18DC 000030 (v01 BOCHS  BXPCRSDT 00000001 BXPC 00000001)",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "ACPI: FACP 0x0000000007FE17B8 000074 (v01 BOCHS  BXPCFACP 00000001 BXPC 00000001)",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "ACPI: DSDT 0x0000000007FE0040 001778 (v01 BOCHS  BXPCDSDT 00000001 BXPC 00000001)",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "ACPI: FACS 0x0000000007FE0000 000040",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "ACPI: APIC 0x0000000007FE182C 000078 (v01 BOCHS  BXPCAPIC 00000001 BXPC 00000001)",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "ACPI: HPET 0x0000000007FE18A4 000038 (v01 BOCHS  BXPCHPET 00000001 BXPC 00000001)",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "ACPI: 1 ACPI AML tables successfully acquired and loaded",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "kworker/u2:0 (32) used greatest stack depth: 14256 bytes left",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "kworker/u2:0 (26) used greatest stack depth: 13920 bytes left",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "acpi PNP0A03:00: fail to add MMCONFIG information, can't access extended PCI configuration space under this bridge.",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "ACPI: Enabled 3 GPEs in block 00 to 0F",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "ACPI: PCI Interrupt Link [LNKC] enabled at IRQ 11",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "mdev (949) used greatest stack depth: 13888 bytes left",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "KABOOM: kaboom_init: WARNING",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     26,
                     "WARNING in kaboom_init()",
                     "/home/bene/work/rtl/test-description/tmp/linux-stable-rt/drivers/misc/kaboom.c",
                     TYPE, "WARNING", Priority.NORMAL);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "KABOOM: kaboom_init: ERR",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "KABOOM: kaboom_init: CRIT",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "KABOOM: kaboom_init: ALERT",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "KABOOM: kaboom_init: EMERG",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);

        annotation = iterator.next();
        checkWarning(annotation,
                     39,
                     "BUG in ()",
                     "/home/bene/work/rtl/test-description/tmp/linux-stable-rt/drivers/misc/kaboom.c",
                     TYPE, "BUG", Priority.HIGH);
        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "sysrq: SysRq : Emergency Sync",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);
        annotation = iterator.next();
        checkWarning(annotation,
                     0,
                     "sysrq: SysRq : Emergency Remount R/O",
                     "Nil",
                     TYPE, "Kernel Output", Priority.LOW);
    }

    @Override
    protected String getWarningsFile() {
        return "kernel.log";
    }
}
