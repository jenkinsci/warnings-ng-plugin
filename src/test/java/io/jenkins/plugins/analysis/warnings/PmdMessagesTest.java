package io.jenkins.plugins.analysis.warnings;

import org.junit.jupiter.api.Test;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

/**
 * Tests the class {@link PmdMessages}.
 *
 * @author Ullrich Hafner
 */
class PmdMessagesTest {
    private static final int EXPECTED_RULE_SETS_SIZE = 8;

    @Test
    void shouldInitializeRuleSets() {
        PmdMessages messages = new PmdMessages();
        assertThat(messages.initialize())
                .as("Wrong number of rule sets found")
                .isEqualTo(EXPECTED_RULE_SETS_SIZE);

        assertThat(messages.getMessage("Error Prone", "NullAssignment"))
                .contains("Assigning a \"null\" to a variable (outside of its declaration) is usually bad form.");
    }
}