package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.WarningsDescriptor;
import hudson.plugins.warnings.util.XmlElementUtil;
import hudson.util.IOException2;
import org.apache.commons.lang.StringEscapeUtils;

import hudson.Extension;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A parser for IntelliJ IDEA inspections.
 *
 * @author Alex Lopashev, alexlopashev@gmail.com
 */
@Extension
public class IdeaInspectionParser extends AbstractWarningsParser {
    private static final long serialVersionUID = 3307389086106375473L;

    private static final String IDEA_SMALL_ICON = WarningsDescriptor.IMAGE_PREFIX + "idea-24x24.png";
    private static final String IDEA_LARGE_ICON = WarningsDescriptor.IMAGE_PREFIX + "idea-48x48.png";

    /**
     * Creates a new instance of {@link IdeaInspectionParser}.
     */
    public IdeaInspectionParser() {
        super(Messages._Warnings_IdeaInspection_ParserName(),
                Messages._Warnings_IdeaInspection_LinkName(),
                Messages._Warnings_IdeaInspection_TrendName());
    }

    @Override
    public Collection<FileAnnotation> parse(Reader reader) throws IOException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(reader));

            Element rootElement = (Element) document.getElementsByTagName("problems").item(0);
            return parseProblems(XmlElementUtil.getNamedChildElements(rootElement, "problem"));
        }
        catch (ParserConfigurationException exception) {
            throw new IOException2(exception);
        }
        catch (SAXException exception) {
            throw new IOException2(exception);
        }
    }

    private List<FileAnnotation> parseProblems(List<Element> elements) {
        List<FileAnnotation> problems = Lists.newArrayList();
        for (Element element : elements) {
            String file = getChildValue(element, "file");
            int line = Integer.parseInt(getChildValue(element, "line"));
            Element problemClass = XmlElementUtil.getFirstElementByTagName(element, "problem_class");
            String severity = problemClass.getAttribute("severity");
            String category = StringEscapeUtils.unescapeXml(getValue(problemClass));
            String description = StringEscapeUtils.unescapeXml(getChildValue(element, "description"));
            problems.add(createWarning(file, line, category, description, getPriority(severity)));
        }
        return problems;
    }

    private Priority getPriority(String severity) {
        Priority priority = Priority.LOW;
        if (severity.equals("WARNING"))
            priority = Priority.NORMAL;
        else if (severity.equals("ERROR"))
            priority = Priority.HIGH;
        return priority;
    }

    private String getValue(Element element) {
        return element.getFirstChild().getNodeValue();
    }

    private String getChildValue(Element element, String childTag) {
        return XmlElementUtil.getFirstElementByTagName(element, childTag).getFirstChild().getNodeValue();
    }

    @Override
    protected String getId() {
        return "IntelliJ IDEA Inspections";
    }

    @Override
    public String getSmallImage() {
        return IDEA_SMALL_ICON;
    }

    @Override
    public String getLargeImage() {
        return IDEA_LARGE_ICON;
    }
}

