package hudson.plugins.warnings.parser;

import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Handles parsing.
 */
public class JSLintXMLSaxParser extends DefaultHandler {
    private static final Logger LOGGER = Logger.getLogger(JSLintXMLSaxParser.class.toString());
    private final List<FileAnnotation> warnings;
    private String fileName;
    private final String type;

    /** Categories. */
    private static final String CATEGORY_PARSING = "Parsing";
    private static final String CATEGORY_UNDEFINED_VARIABLE = "Undefined Variable";
    private static final String CATEGORY_FORMATTING = "Formatting";

    /**
     * Creates a new instance of {@link JSLintXMLSaxParser}.
     *
     * @param type
     *            type of the parser
     * @param warnings
     *            the warnings output
     */
    public JSLintXMLSaxParser(final String type, final List<FileAnnotation> warnings) {
        super();

        this.type = type;
        this.warnings = warnings;
    }

    @Override
    public void startElement(final String namespaceURI, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        String key = qName;

        if (isLintDerivate(key)) {
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
                category = CATEGORY_PARSING;
            }
            else if (message.endsWith(" is not defined.")) {
                priority = Priority.HIGH;
                category = CATEGORY_UNDEFINED_VARIABLE;
            }
            else if (message.contains("Mixed spaces and tabs")) {
                priority = Priority.LOW;
                category = CATEGORY_FORMATTING;
            }
            else if (message.contains("Unnecessary semicolon")) {
                category = CATEGORY_FORMATTING;
            }
            else if (message.contains("is better written in dot notation")) {
                category = CATEGORY_FORMATTING;
            }

            int lineNumber = AbstractWarningsParser.convertLineNumber(atts.getValue("line"));
            Warning warning = new Warning(fileName, lineNumber, type, category, message, priority);

            warnings.add(warning);
            return;
        }
        else {
            LOGGER.info("Unknown jslint xml tag: " + key);
        }
    }

    private boolean isLintDerivate(final String key) {
        return key != null && key.contains("lint");
    }
}
