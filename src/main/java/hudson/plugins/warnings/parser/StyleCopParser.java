package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.util.XmlElementUtil;

import hudson.util.IOException2;

/**
 * Parses a StyleCop (http://code.msdn.microsoft.com/sourceanalysis/) xml report file.
 *
 * @author Sebastian Seidl
 */
public class StyleCopParser extends AbstractWarningsParser {
    private static final long serialVersionUID = 1L;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    private transient List<FileAnnotation> warnings;

    /**
     * Creates a new instance of {@link StyleCopParser}.
     */
    public StyleCopParser() {
        super(Messages._Warnings_StyleCop_ParserName(),
                Messages._Warnings_StyleCop_LinkName(),
                Messages._Warnings_StyleCop_TrendName());
    }

    @Override
    public Collection<FileAnnotation> parse(final Reader reader) throws IOException, ParsingCanceledException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            warnings = Lists.newArrayList();

            docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(reader));

            // Pre v4.3 uses SourceAnalysisViolations as the parent node name
            NodeList mainNode = doc.getElementsByTagName("SourceAnalysisViolations");
            if (mainNode.getLength() == 0) {
                // v4.3 uses StyleCopViolations as the parent node name
                mainNode = doc.getElementsByTagName("StyleCopViolations");
            }

            Element rootElement = (Element) mainNode.item(0);
            parseViolations(XmlElementUtil.getNamedChildElements(rootElement, "Violation"));
        }
        catch (ParserConfigurationException pce) {
            throw new IOException2(pce);
        }
        catch (SAXException se) {
            throw new IOException2(se);
        }

        return warnings;
    }

    /**
     * Parse the Violation tag and add it to the warnings.
     * @param elements list of Violation tags
     */
    private void parseViolations(final List<Element> elements) {
        for (Element element : elements) {
            Warning warning = createWarning(getString(element, "Source"),                getLineNumber(element),
                getString(element, "Rule"),
                getCategory(element),
                element.getTextContent(),
                Priority.NORMAL);

                warnings.add(warning);
        }
    }

    /**
     * Returns the Category of a StyleCop Violation.
     * @param element The Element which represents the violation
     * @return Category of violation
     */
    private String getCategory(final Element element) {
        String ruleNameSpace = getString(element, "RuleNamespace");

        int i = ruleNameSpace.lastIndexOf('.');
        if (i == -1) {
           return getString(element, "RuleId");
        }
        else {
            return ruleNameSpace.substring(i + 1);
        }
    }

    /***
     * Returns the value for the named attribute if it exists.
     * @param element the element to check for an attribute
     * @param name the name of the attribute
     * @return the value of the attribute; "" if there is no such attribute.
     */
    private String getString(final Element element, final String name) {
        if (element.hasAttribute(name)) {
            return element.getAttribute(name);
        }
        else {
            return "";
        }
    }

    /***
     * Returns the LineNumber for the given violation.
     * @param violation the xml Element "violation" to get the Linenumber from.
     * @return the lineNumber of the violation. -1 if there is no LineNumber or
     * the LineNumber cant't be parsed as an Integer.
     */
    private int getLineNumber(final Element violation) {
        if (violation.hasAttribute("LineNumber")) {
            try {
                return Integer.parseInt(violation.getAttribute("LineNumber"));
            }
            catch (NumberFormatException e) {
                return -1;
            }
        }
        else {
            return -1;
        }
    }
}
