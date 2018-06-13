package io.jenkins.plugins.analysis.warnings.checkstyle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.digester3.NodeCreateRule;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Digester rule to parse the actual content of a DocBook subsection node. Does not interpret XML elements that are
 * children of a subsection.
 *
 * @author Ulli Hafner
 */
public class TopicRule extends NodeCreateRule {
    /**
     * Instantiates a new topic rule.
     *
     * @throws ParserConfigurationException
     *         the parser configuration exception
     */
    public TopicRule() throws ParserConfigurationException {
        super(Node.ELEMENT_NODE);
    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void end(final String namespace, final String name) throws Exception {
        Element subsection = getDigester().pop();
        String description = extractNoteContent(subsection);

        MethodUtils.invokeExactMethod(getDigester().peek(), "setValue", description);
    }

    /**
     * Extracts the node content. Basically returns every character in the subsection element.
     *
     * @param subsection
     *         the subsection of a rule
     *
     * @return the node content
     * @throws ParserConfigurationException
     *         in case of an error
     * @throws IOException
     *         in case of an error
     */
    protected String extractNoteContent(final Element subsection) throws ParserConfigurationException,
            IOException {
        StringWriter writer = new StringWriter();
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();

        OutputFormat format = new OutputFormat(doc);
        format.setOmitXMLDeclaration(true);
        XMLSerializer serializer = new XMLSerializer(writer, format);
        serializer.serialize(subsection);

        String serialized = writer.getBuffer().toString();
        serialized = StringUtils.substringAfter(serialized, ">");
        return StringUtils.substringBeforeLast(serialized, "<");
    }
}