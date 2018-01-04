package io.jenkins.plugins.analysis.warnings.checkstyle;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link CheckStyleRules}.
 *
 * @author Ullrich Hafner
 */
class CheckStyleRulesTest {
    private static final int NUMBER_OF_AVAILABLE_CHECKSTYLE_RULES = 159;

    /** Test whether we could parse the Checkstyle rule meta data. */
    @Test
   void shouldLoadAndParseAllRules() {
        CheckStyleRules rules = new CheckStyleRules();
        rules.initialize();

        assertThat(rules.getRules()).hasSize(NUMBER_OF_AVAILABLE_CHECKSTYLE_RULES);
        assertThat(rules.getRule("EmptyBlock")).as("No rule information found").isNotNull();
        assertThat(rules.getRule("EmptyBlock").getDescription()).as("Wrong description for EmptyBlock found.").isEqualTo("<p> Checks for empty blocks. </p>");
        assertThat(rules.getRule("AnnotationUseStyle")).as("No rule information found").isNotNull();
        assertThat(rules.getRule("AnnotationUseStyle").getDescription()).as("Wrong description for AnnotationUseStyle found.").contains("This check controls the style with the usage of annotations.");
        assertThat(rules.getRule("Undefined").getDescription()).as("No default text available for undefined rule.").isEqualTo(Rule.UNDEFINED_DESCRIPTION);

        for (Rule rule : rules.getRules()) {
            assertThat(rule.getDescription()).as("Rule %s has no description", rule.getName()).isNotEqualTo(Rule.UNDEFINED_DESCRIPTION);
        }
    }
}