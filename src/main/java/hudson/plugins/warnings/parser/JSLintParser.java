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
public class JSLintParser extends AbstractWarningsParser implements WarningsParser {

    private static final long serialVersionUID = 8613418992526753095L;

    static final Logger logger = Logger.getLogger(JSLintParser.class.toString());

    /** Categories */
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

    /** {@inheritDoc} */
    public String getName() {
        return getGroup();
    }

    class JSLintXMLSaxParser extends DefaultHandler
    {
        final List<FileAnnotation> warnings;
        private String fileName;
        public JSLintXMLSaxParser(final List<FileAnnotation> warnings)
        {
            this.warnings = warnings;
        }
        @Override
        public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) throws SAXException {
            String key = qName;

            // Start element, good to skip
            if (key.equals("jslint")) { return; }

            if (key.equals("file"))
            {
                fileName = atts.getValue("name");
                return;
            }
            if (key.equals("issue"))
            {
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

                Warning warning = createWarning(
                        fileName,
                        Integer.parseInt(atts.getValue("line")),
                        category,
                        message,
                        priority
                );

                warnings.add(warning);
                return;
            }

            logger.info("Unknown jslint xml tag: " + key );
        }
    }

    @Override
    public Collection<FileAnnotation> parse(final Reader file) throws IOException, ParsingCanceledException {

        ArrayList<FileAnnotation> warnings = new ArrayList<FileAnnotation>();

        ReaderInputStream ris = new ReaderInputStream(file,"UTF-8");
        JSLintXMLSaxParser parser = new JSLintXMLSaxParser(warnings);

      //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(ris, parser);

        }catch(SAXException se) {
            se.printStackTrace();
        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }catch (IOException ie) {
            ie.printStackTrace();
        }

        return warnings;
    }
}

