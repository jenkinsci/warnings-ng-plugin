package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests that verify the XML parser infrastructure warm-up logic introduced to fix JENKINS-66268.
 *
 * @author Akash Manna
 * @see <a href="https://issues.jenkins.io/browse/JENKINS-66268">JENKINS-66268</a>
 */
class XmlParserWarmUpTest {
    private static final String SIMPLE_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project>
              <name>TestModule</name>
            </project>
            """;

    /**
     * Verifies that calling {@code SAXParserFactory.newInstance()} followed by
     * {@code setFeature(FEATURE_SECURE_PROCESSING, true)} does not throw any exception.
     */
    @Test
    @Issue("JENKINS-66268")
    void shouldCreateSaxParserFactoryWithSecureProcessingWithoutException()
            throws SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException {
        var factory = SAXParserFactory.newInstance();

        assertThatCode(() -> factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true))
                .doesNotThrowAnyException();
    }

    /**
     * Verifies that calling {@code DocumentBuilderFactory.newInstance()} followed by
     * {@code setFeature(FEATURE_SECURE_PROCESSING, true)} does not throw any exception.
     */
    @Test
    @Issue("JENKINS-66268")
    void shouldCreateDocumentBuilderFactoryWithSecureProcessingWithoutException() {
        var factory = DocumentBuilderFactory.newInstance();

        assertThatCode(() -> factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true))
                .doesNotThrowAnyException();
    }

    /**
     * Verifies that after the warm-up, a {@link SAXParser} can still be created and used
     * to parse XML — confirming the warm-up does not corrupt the factory state.
     */
    @Test
    @Issue("JENKINS-66268")
    void shouldProduceUsableSaxParserAfterWarmUp()
            throws SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException {
        var factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        assertThatCode(() -> {
            SAXParser parser = factory.newSAXParser();
            assertThat(parser).isNotNull();
            assertThat(parser.isValidating()).isFalse();
        }).doesNotThrowAnyException();
    }

    /**
     * Verifies that after the warm-up, a {@link DocumentBuilder} can still be created and used 
     * to parse XML — confirming the warm-up does not corrupt the factory state.
     */
    @Test
    @Issue("JENKINS-66268")
    void shouldProduceUsableDocumentBuilderAfterWarmUp()
            throws ParserConfigurationException {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        assertThatCode(() -> {
            DocumentBuilder builder = factory.newDocumentBuilder();
            var document = builder.parse(
                    new ByteArrayInputStream(SIMPLE_XML.getBytes(StandardCharsets.UTF_8)));
            assertThat(document.getElementsByTagName("name").item(0).getTextContent())
                    .isEqualTo("TestModule");
        }).doesNotThrowAnyException();
    }
}
