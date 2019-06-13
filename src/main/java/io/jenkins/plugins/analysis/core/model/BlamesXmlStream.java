package io.jenkins.plugins.analysis.core.model;

import hudson.util.XStream2;

import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileBlame;

/**
 * Reads {@link Blames} from an XML file.
 *
 * @author Ullrich Hafner
 */
 class BlamesXmlStream extends AbstractXmlStream<Blames> {
    BlamesXmlStream() {
        super(Blames.class);
    }

    @Override
    protected Blames createDefaultValue() {
        return new Blames();
    }

    @Override
    protected XStream2 createStream() {
        XStream2 xStream2 = new XStream2();
        xStream2.alias("io.jenkins.plugins.analysis.core.scm.Blames", Blames.class);
        xStream2.alias("io.jenkins.plugins.analysis.core.scm.BlameRequest", FileBlame.class);
        xStream2.alias("blames", Blames.class);
        xStream2.alias("blame", FileBlame.class);
        return xStream2;
    }

}
