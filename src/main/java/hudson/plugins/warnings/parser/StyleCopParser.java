package hudson.plugins.warnings.parser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import hudson.Extension;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.util.XmlElementUtil;

/**
 * Parses a StyleCop (http://code.msdn.microsoft.com/sourceanalysis/) xml report file.
 *
 * @author Sebastian Seidl
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class StyleCopParser extends AbstractWarningsParser {
    private static final long serialVersionUID = 1L;

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

            docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(reader));

            // Pre v4.3 uses SourceAnalysisViolations as the parent node name
            NodeList mainNode = doc.getElementsByTagName("SourceAnalysisViolations");
            if (mainNode.getLength() == 0) {
                // v4.3 uses StyleCopViolations as the parent node name
                mainNode = doc.getElementsByTagName("StyleCopViolations");
            }

            Element rootElement = (Element)mainNode.item(0);
            return parseViolations(XmlElementUtil.getNamedChildElements(rootElement, "Violation"));
        }
        catch (ParserConfigurationException exception) {
            throw new IOException(exception);
        }
        catch (SAXException exception) {
            throw new IOException(exception);
        }
    }

    /**
     * Parses the "Violation" tag and adds one warning for each element.
     *
     * @param elements
     *            list of Violation tags
     * @return the corresponding warnings
     */
    private Collection<FileAnnotation> parseViolations(final List<Element> elements) {
        Collection<FileAnnotation> warnings = Lists.newArrayList();
        for (Element element : elements) {
            Warning warning = createWarning(getString(element, "Source"), getLineNumber(element),
                    getString(element, "Rule"), getCategory(element), element.getTextContent(), Priority.NORMAL);

            warnings.add(warning);
        }
        return warnings;
    }

    /**
     * Returns the Category of a StyleCop Violation.
     *
     * @param element
     *            The Element which represents the violation
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
     *
     * @param element
     *            the element to check for an attribute
     * @param name
     *            the name of the attribute
     * @return the value of the attribute; "" if there is no such attribute.
     */
    private String getString(final Element element, final String name) {
        if (element.hasAttribute(name)) {
            return element.getAttribute(name);
        }
        else {
            return StringUtils.EMPTY;
        }
    }

    /***
     * Returns the LineNumber for the given violation.
     *
     * @param violation
     *            the xml Element "violation" to get the Linenumber from.
     * @return the lineNumber of the violation. 0 if there is no LineNumber or the LineNumber cant't be parsed into an
     *         Integer.
     */
    private int getLineNumber(final Element violation) {
        if (violation.hasAttribute("LineNumber")) {
            return getLineNumber(violation.getAttribute("LineNumber"));
        }
        else {
            return 0;
        }
    }
}
