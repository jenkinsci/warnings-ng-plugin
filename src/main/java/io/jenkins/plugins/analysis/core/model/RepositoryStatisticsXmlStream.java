package io.jenkins.plugins.analysis.core.model;

import hudson.util.XStream2;

import io.jenkins.plugins.forensics.miner.FileStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.util.AbstractXmlStream;

/**
 * Reads {@link RepositoryStatistics} from an XML file.
 *
 * @author Ullrich Hafner
 */
class RepositoryStatisticsXmlStream extends AbstractXmlStream<RepositoryStatistics> {
    RepositoryStatisticsXmlStream() {
        super(RepositoryStatistics.class);
    }

    @Override
    public RepositoryStatistics createDefaultValue() {
        return new RepositoryStatistics();
    }

    @Override
    protected void configureXStream(final XStream2 xStream) {
        xStream.alias("repo", RepositoryStatistics.class);
        xStream.alias("file", FileStatistics.class);
    }
}
