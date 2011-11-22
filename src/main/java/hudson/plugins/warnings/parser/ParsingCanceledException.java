package hudson.plugins.warnings.parser;

import java.io.IOException;

/**
 * Indicates that parsing has been canceled due to a user initiated interrupt.
 *
 * @author Ulli Hafner
 */
public class ParsingCanceledException extends IOException {
    private static final long serialVersionUID = 3341274949787014225L;

    /**
     * Creates a new instance of {@link ParsingCanceledException}.
     */
    public ParsingCanceledException() {
        super("Canceling parsing since build has been aborted.");
    }
}

