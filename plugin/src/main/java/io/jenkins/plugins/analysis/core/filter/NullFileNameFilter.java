package io.jenkins.plugins.analysis.core.filter;

import edu.hm.hafner.analysis.Issue;
import java.util.Collections;

/**
 * A null object implementation of {@link FileNameFilter} that accepts every issue. It is used when no file
 * inclusion list has been configured, so that the report is passed through unchanged.
 *
 * @author Michael Trimarchi
 */
public class NullFileNameFilter extends FileNameFilter {
    /**
     * Creates a new instance of {@link NullFileNameFilter}.
     */
    public NullFileNameFilter() {
        super(Collections.emptyList());
    }

    @Override
    public boolean test(final Issue issue) {
        return true;
    }
}
