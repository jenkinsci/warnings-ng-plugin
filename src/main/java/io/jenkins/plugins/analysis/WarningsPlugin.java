package io.jenkins.plugins.analysis;

import io.jenkins.plugins.analysis.warnings.PmdMessages;

import hudson.Plugin;

/**
 * Entry point for the warnings plug-in. Currently only used to read the PMD rules, that are loaded from the PMD library
 * using the {@link java.util.ServiceLoader} with the default class loader. If this code is executed later on using the
 * plug-in class loader then no message rules are found.
 *
 * @author Ullrich Hafner
 * @see net.sourceforge.pmd.lang.LanguageRegistry
 */
@SuppressWarnings("deprecation")
public class WarningsPlugin extends Plugin {
    @Override
    public void start() {
        PmdMessages messages = new PmdMessages();
        messages.initialize();
    }
}
