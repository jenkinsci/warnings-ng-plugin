package io.jenkins.plugins.analysis.core.model;

import hudson.util.XStream2;

import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.blame.FileBlame;
import io.jenkins.plugins.util.AbstractXmlStream;

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
    public Blames createDefaultValue() {
        return new Blames();
    }

    @Override
    protected void configureXStream(final XStream2 xStream) {
        xStream.alias("io.jenkins.plugins.analysis.core.scm.Blames", Blames.class);
        xStream.alias("io.jenkins.plugins.analysis.core.scm.BlameRequest", FileBlame.class);
        xStream.alias("blames", Blames.class);
        xStream.alias("blame", FileBlame.class);
    }
}
