package hudson.plugins.warnings;

import java.util.ServiceLoader;

import io.jenkins.plugins.analysis.warnings.PmdMessages;
import net.sourceforge.pmd.lang.LanguageRegistry;

import hudson.Plugin;

/**
 * Entry point for the warnings plug-in. Currently only used to read the PMD rules, that are loaded from the PMD library
 * using the {@link ServiceLoader} with the default class loader. If this code is executed later on using the plug-in
 * class loader then no message rules are found.
 *
 * @author Ullrich Hafner
 * @see LanguageRegistry
 */
@SuppressWarnings("deprecation")
public class WarningsPlugin extends Plugin {
    @Override
    public void start() {
        PmdMessages messages = new PmdMessages();
        messages.initialize();
    }
}
