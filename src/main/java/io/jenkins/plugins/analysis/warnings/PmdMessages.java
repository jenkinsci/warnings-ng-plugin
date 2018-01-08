package io.jenkins.plugins.analysis.warnings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;

/**
 * Provides access to rule descriptions and examples.
 *
 * @author Ulli Hafner
 */
public class PmdMessages {
    private static final String ERROR_MESSAGE = "Installation problem: can't access PMD messages.";
    private static final Logger LOGGER = Logger.getLogger(PmdMessages.class.getName());

    private final Map<String, RuleSet> rules = new HashMap<>();

    /**
     * Initializes the rules.
     *
     * @return the number of rule sets
     */
    public int initialize() {
        try {
            Iterator<RuleSet> ruleSets = new RuleSetFactory().getRegisteredRuleSets();
            for (; ruleSets.hasNext();) {
                RuleSet ruleSet = ruleSets.next();
                rules.put(ruleSet.getName(), ruleSet);
            }
            if (rules.isEmpty()) {
                LOGGER.log(Level.SEVERE, ERROR_MESSAGE);
            }
            return rules.size();
        }
        catch (RuleSetNotFoundException exception) {
            LOGGER.log(Level.SEVERE, ERROR_MESSAGE, exception);
        }
        return 0;
    }

    /**
     * Returns the message for the specified PMD rule.
     *
     * @param ruleSetName
     *            PMD rule set
     * @param ruleName
     *            PMD rule ID
     * @return the message
     */
    public String getMessage(final String ruleSetName, final String ruleName) {
        if (rules.containsKey(ruleSetName)) {
            RuleSet ruleSet = rules.get(ruleSetName);
            Rule rule = ruleSet.getRuleByName(ruleName);
            if (rule != null) {
                return createMessage(rule);
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Creates the message string to be shown for the specified rule.
     *
     * @param rule
     *            the rule
     * @return the message string to be shown for the specified rule
     */
    private String createMessage(final Rule rule) {
        List<String> examples = rule.getExamples();
        if (!examples.isEmpty()) {
            return rule.getDescription() + "<pre>" + examples.get(0) + "</pre>";
        }
        return rule.getDescription();
    }
}

