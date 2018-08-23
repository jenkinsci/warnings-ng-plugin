package hudson.plugins.warnings.parser;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tools.ant.util.ReaderInputStream;
import org.jvnet.localizer.Localizable;
import org.xml.sax.SAXException;

import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Base class for parsers based on {@link JSLintXMLSaxParser}.
 *
 * @author Ullrich Hafner
 * @deprecated use the new analysis-model library
 */
@Deprecated
public abstract class LintParser extends AbstractWarningsParser {
    private static final long serialVersionUID = 3341424685245834156L;

    /**
     * Creates a new instance of {@link LintParser}.
     *
     * @param parserName
     *            name of the parser
     * @param linkName
     *            name of the project action link
     * @param trendName
     *            name of the trend graph
     */
    protected LintParser(final Localizable parserName, final Localizable linkName, final Localizable trendName) {
        super(parserName, linkName, trendName);
    }

    @Override
    public Collection<FileAnnotation> parse(final Reader file) throws IOException, ParsingCanceledException {
        try {
            List<FileAnnotation> warnings = new ArrayList<FileAnnotation>();
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();

            SAXParser parser = parserFactory.newSAXParser();
            parser.parse(new ReaderInputStream(file, "UTF-8"), new JSLintXMLSaxParser(getGroup(), warnings));

            return warnings;
        }
        catch (SAXException exception) {
            throw new IOException(exception);
        }
        catch (ParserConfigurationException exception) {
            throw new IOException(exception);
        }
    }
}
