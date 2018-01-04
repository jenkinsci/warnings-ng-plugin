package io.jenkins.plugins.analysis.warnings;

import org.junit.jupiter.api.Test;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

/**
 * Tests the class {@link PmdMessages}.
 *
 * @author Ullrich Hafner
 */
class PmdMessagesTest {
    /**
     * Verifies that the PMD messages could be correctly read.
     */
    @Test
    public void shouldHaveAllMessage() {
        PmdMessages messages = new PmdMessages();
        assertThat(messages.initialize()).as("Wrong number of rulesets found").isEqualTo(25);

        assertThat(messages.getMessage("Empty Code", "EmptyCatchBlock")).isEqualTo("\n"
                + "Empty Catch Block finds instances where an exception is caught, but nothing is done.  \n"
                + "In most circumstances, this swallows an exception which should either be acted on \n"
                + "or reported.\n"
                + "      <pre>\n"
                + "  \n"
                + "public void doSomething() {\n"
                + "  try {\n"
                + "    FileInputStream fis = new FileInputStream(\"/tmp/bugger\");\n"
                + "  } catch (IOException ioe) {\n"
                + "      // not good\n"
                + "  }\n"
                + "}\n"
                + " \n"
                + "      </pre>");
    }
}