package hudson.plugins.warnings.parser;

import java.io.IOException;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.ProxyWhitelist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.StaticWhitelist;

import hudson.Extension;

/**
 * Registers a whitelist from the plug-in resource {@link #WHITELIST_FILE_NAME}.
 *
 * @author Ullrich Hafner
 */
@Extension
public class GroovyWhiteList extends ProxyWhitelist {
    private static final String WHITELIST_FILE_NAME = "groovy.whitelist";

    public GroovyWhiteList() throws IOException {
        super(StaticWhitelist.from(GroovyWhiteList.class.getResource(WHITELIST_FILE_NAME)));
    }
}
