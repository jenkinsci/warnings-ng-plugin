package io.jenkins.plugins.analysis.warnings.checkstyle;

import java.io.StringWriter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.digester3.NodeCreateRule;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Digester rule to parse the actual content of a DocBook subsection node. Does not interpret XML elements that are
 * children of a subsection.
 *
 * @author Ullrich Hafner
 */
public class TopicRule extends NodeCreateRule {
    /**
     * Instantiates a new topic rule.
     *
     * @throws ParserConfigurationException
     *         the parser configuration exception
     */
    TopicRule() throws ParserConfigurationException {
        super(Node.ELEMENT_NODE);
    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void end(final String namespace, final String name) throws Exception {
        Element subsection = getDigester().pop();
        String description = extractNodeContent(subsection);

        MethodUtils.invokeExactMethod(getDigester().peek(), "setValue", description);
    }

    /**
     * Extracts the node content. Basically returns every character in the subsection element.
     *
     * @param subsection
     *         the subsection of a rule
     *
     * @return the node content
     * @throws TransformerException
     *         in case of an error
     */
    private String extractNodeContent(final Element subsection) throws TransformerException {
        StringWriter content = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(subsection), new StreamResult(content));
        String text = content.toString();
        String prefixRemoved = StringUtils.substringAfter(text, ">");
        String suffixRemoved = StringUtils.substringBeforeLast(prefixRemoved, "<");

        String endSourceRemoved = StringUtils.replace(suffixRemoved, "</source>", "</code></pre>");

        return StringUtils.replace(endSourceRemoved, "<source>", "<pre><code>");
    }
}