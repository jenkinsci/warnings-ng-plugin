package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Decorator of <code>WarningsParser</code> instances. Makes instances of the old
 * extension point look like instances of the new extension point.
 *
 * @author Ulli Hafner
 */
public class ParserAdapter extends AbstractWarningsParser {
    private static final long serialVersionUID = 1591875174941046868L;
    @SuppressWarnings("deprecation")
    private final WarningsParser wrapped;

    /**
     * Creates a new instance of {@link ParserAdapter}.
     *
     * @param wrapped
     *            the wrapped parser
     */
    @SuppressWarnings("deprecation")
    public ParserAdapter(final WarningsParser wrapped) {
        super(wrapped.getName());

        this.wrapped = wrapped;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Collection<FileAnnotation> parse(final Reader reader) throws IOException, ParsingCanceledException {
        return wrapped.parse(reader);
    }
}

