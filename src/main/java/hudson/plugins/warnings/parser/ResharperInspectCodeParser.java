package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.jvnet.localizer.Localizable;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.util.XmlElementUtil;
import hudson.plugins.warnings.WarningsDescriptor;

/**
 * A parser for the Resharper InspectCode compiler warnings.
 *
 * @author Rafal Jasica
 */
@Extension
public class ResharperInspectCodeParser extends RegexpLineParser {
    private static final String RESHAPER_SMALL_ICON = WarningsDescriptor.IMAGE_PREFIX + "resharper-24x24.png";
    private static final String RESHAPER_LARGE_ICON = WarningsDescriptor.IMAGE_PREFIX + "resharper-48x48.png";

    private static final long serialVersionUID = 526872513348892L;
    private static final String WARNING_TYPE = "ResharperInspectCode";
    private static final String WARNING_PATTERN = "\\<Issue.*?TypeId=\"(.*?)\".*?File=\"(.*?)\".*?Line=\"(.*?)\".*?Message=\"(.*?)\"";
    
    private final Map<String, Priority> priorityByTypeId = new HashMap<String, Priority>();

    /**
     * Creates a new instance of {@link ResharperInspectCodeParser}.
     */
    public ResharperInspectCodeParser() {
        this(Messages._Warnings_ReshaperInspectCode_ParserName(),
                Messages._Warnings_ReshaperInspectCode_LinkName(),
                Messages._Warnings_ReshaperInspectCode_TrendName());
    }

    /**
     * Creates a new instance of {@link ResharperInspectCodeParser}.
     *
     * @param parserName
     *            name of the parser
     * @param linkName
     *            name of the project action link
     * @param trendName
     *            name of the trend graph
     */
    public ResharperInspectCodeParser(final Localizable parserName, final Localizable linkName, final Localizable trendName) {
        super(parserName, linkName, trendName, WARNING_PATTERN, true);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        return createWarning(
            matcher.group(2),
            getLineNumber(matcher.group(3)),
            WARNING_TYPE,
            matcher.group(1),
            matcher.group(4),
            GetPriority(matcher.group(1)));
    }

    @Override
    protected String getId() {
        return "Reshaper InspectCode";
    }

    @Override
    public String getSmallImage() {
        return RESHAPER_SMALL_ICON;
    }

    @Override
    public String getLargeImage() {
        return RESHAPER_LARGE_ICON;
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        if (line.contains("<IssueType Id=")){
            try {
                // This is a quick workaround to get the IssueType parsing
                // to work for this parser (which is a RegexpLineParser)
                // It should probably be entirely xml-based instead
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder;
                docBuilder = docBuilderFactory.newDocumentBuilder();
                
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader("<IssueTypes>" + line + "</IssueTypes>"));
                Document doc = docBuilder.parse(is);
                
                NodeList mainNode = doc.getElementsByTagName("IssueTypes");
                Element issueTypesElement = (Element)mainNode.item(0);
                parseIssueTypes(XmlElementUtil.getNamedChildElements(issueTypesElement, "IssueType"));
            } catch (ParserConfigurationException ex) {
            } catch (SAXException ex) {
            } catch (IOException ex) {
            }
            return false;
        }        
        return line.contains("<Issue");
    }
    
    private void parseIssueTypes(final List<Element> issueTypeElements) {
        for (Element issueTypeElement : issueTypeElements) {
            String id = issueTypeElement.getAttribute("Id");
            if (!"".equals(id)) {
                String severity = issueTypeElement.getAttribute("Severity");
                if ("ERROR".equals(severity)){
                    priorityByTypeId.put(id, Priority.HIGH);
                }
                else if ("WARNING".equals(severity)) {
                    priorityByTypeId.put(id, Priority.NORMAL);
                }
                else if ("SUGGESTION".equals(severity)) {
                    priorityByTypeId.put(id, Priority.LOW);
                }
            }
        }
    }
    
    private Priority GetPriority(final String typeId)
    {
        if (priorityByTypeId.containsKey(typeId)) {
            return priorityByTypeId.get(typeId); 
        }
        else {
            return Priority.NORMAL;
        }
    }
}
