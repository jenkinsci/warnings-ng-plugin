package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.FileAnnotation;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

/**
 * Decorator of {@link WarningsParser} instances. Makes instances of the old
 * extension point look like instances of the new extension point.
 *
 * @author Ulli Hafner
 */
@SuppressWarnings({"deprecation", "javadoc"})
public class ParserAdapter extends AbstractWarningsParser {
    private static final long serialVersionUID = 1591875174941046868L;
    private final WarningsParser wrapped;

    /**
     * Creates a new instance of {@link ParserAdapter}.
     *
     * @param wrapped
     *            the wrapped parser
     */
    public ParserAdapter(final WarningsParser wrapped) {
        super(wrapped.getName());

        this.wrapped = wrapped;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<FileAnnotation> parse(final Reader reader) throws IOException, ParsingCanceledException {
        return wrapped.parse(reader);
    }
}

