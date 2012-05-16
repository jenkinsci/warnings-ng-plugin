package hudson.plugins.warnings.parser;

import hudson.Extension;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.util.ReaderInputStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A parser for JSLint checks warnings.
 *
 * @author Gavin Mogan <gavin@kodekoan.com>
 */
@Extension
public class JSLintParser extends AbstractWarningsParser {
    private static final long serialVersionUID = 8613418992526753095L;
    private static final Logger LOGGER = Logger.getLogger(JSLintParser.class.toString());

    /** Categories. */
    static final String CATEGORY_PARSING = "Parsing";
    static final String CATEGORY_UNDEFINED_VARIABLE = "Undefined Variable";
    static final String CATEGORY_FORMATTING = "Formatting";

    /**
     * Creates a new instance of {@link JSLintParser}.
     */
    public JSLintParser() {
        super(Messages._Warnings_JSLint_ParserName(),
                Messages._Warnings_JSLint_LinkName(),
                Messages._Warnings_JSLint_TrendName());
    }

    @Override
    public Collection<FileAnnotation> parse(final Reader file) throws IOException, ParsingCanceledException {
        try {
            List<FileAnnotation> warnings = new ArrayList<FileAnnotation>();
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();

            SAXParser parser = parserFactory.newSAXParser();
            parser.parse(new ReaderInputStream(file, "UTF-8"), new JSLintXMLSaxParser(warnings));

            return warnings;
        }
        catch (SAXException exception) {
            throw new IOException(exception);
        }
        catch (ParserConfigurationException exception) {
            throw new IOException(exception);
        }
    }

    /**
     * Handles parsing.
     */
    private class JSLintXMLSaxParser extends DefaultHandler {
        private final List<FileAnnotation> warnings;
        private String fileName;

        public JSLintXMLSaxParser(final List<FileAnnotation> warnings) {
            super();

            this.warnings = warnings;
        }

        @Override
        public void startElement(final String namespaceURI, final String localName,
                final String qName, final Attributes atts) throws SAXException {
            String key = qName;

            if ("jslint".equals(key)) {
                return; // Start element, good to skip
            }
            if ("file".equals(key)) {
                fileName = atts.getValue("name");
                return;
            }
            if ("issue".equals(key)) {
                String category = StringUtils.EMPTY;
                Priority priority = Priority.NORMAL;

                String message = atts.getValue("reason");
                if (message.startsWith("Expected")) {
                    priority = Priority.HIGH;
                    category = JSLintParser.CATEGORY_PARSING;
                }
                else if (message.endsWith(" is not defined.")) {
                    priority = Priority.HIGH;
                    category = JSLintParser.CATEGORY_UNDEFINED_VARIABLE;
                }
                else if (message.contains("Mixed spaces and tabs")) {
                    priority = Priority.LOW;
                    category = JSLintParser.CATEGORY_FORMATTING;
                }
                else if (message.contains("Unnecessary semicolon")) {
                    category = JSLintParser.CATEGORY_FORMATTING;
                }
                else if (message.contains("is better written in dot notation")) {
                    category = JSLintParser.CATEGORY_FORMATTING;
                }

                int lineNumber = getLineNumber(atts.getValue("line"));
                Warning warning = createWarning( fileName, lineNumber, category, message, priority);

                warnings.add(warning);
                return;
            }
            else {
                LOGGER.info("Unknown jslint xml tag: " + key);
            }
        }
    }
}
