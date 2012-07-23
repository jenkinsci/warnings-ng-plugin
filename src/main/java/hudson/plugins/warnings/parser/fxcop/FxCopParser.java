package hudson.plugins.warnings.parser.fxcop;

import hudson.Extension;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.parser.AbstractWarningsParser;
import hudson.plugins.warnings.parser.Messages;
import hudson.plugins.warnings.parser.ParsingCanceledException;
import hudson.plugins.warnings.parser.Warning;
import hudson.util.IOException2;

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

/**
 * Parses a fxcop xml report file. This does not uses the XML Pull parser as it
 * can not handle the FxCop XML files. The bug is registered at Sun as http:
 * //bugs.sun.com/bugdatabase/view_bug.do?bug_id=4508058
 * <p>
 * Note that instances of this parser are not thread safe.
 * </p>
 */
@SuppressWarnings("unused")
@Extension
public class FxCopParser extends AbstractWarningsParser {
    private static final long serialVersionUID = -7208558002331355408L;

    private transient FxCopRuleSet ruleSet;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    private transient List<FileAnnotation> warnings;

    /**
     * Creates a new instance of {@link FxCopParser}.
     */
    public FxCopParser() {
        super(Messages._Warnings_FxCop_ParserName(),
                Messages._Warnings_FxCop_LinkName(),
                Messages._Warnings_FxCop_TrendName());
    }

    @Override
    public Collection<FileAnnotation> parse(final Reader reader)
            throws IOException, ParsingCanceledException {
        try {
            ruleSet = new FxCopRuleSet();
            warnings = Lists.newArrayList();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;
            docBuilder = docBuilderFactory.newDocumentBuilder();

            Document doc = docBuilder.parse(new InputSource(reader));

            NodeList mainNode = doc.getElementsByTagName("FxCopReport");

            Element rootElement = (Element)mainNode.item(0);
            parseRules(XmlElementUtil.getFirstElementByTagName(rootElement, "Rules"));
            parseNamespaces(XmlElementUtil.getFirstElementByTagName(rootElement, "Namespaces"), null);
            parseTargets(XmlElementUtil.getFirstElementByTagName(rootElement, "Targets"));

            return warnings;
        }
        catch (ParserConfigurationException exception) {
            throw new IOException2(exception);
        }
        catch (SAXException exception) {
            throw new IOException2(exception);
        }
    }

    private void parseRules(final Element rulesElement) {
        if (rulesElement != null) {
            for (Element rule : XmlElementUtil.getNamedChildElements(rulesElement, "Rule")) {
                ruleSet.addRule(rule);
            }
        }
    }

    private void parseTargets(final Element targetsElement) {
        if (targetsElement != null) {
            for (Element target : XmlElementUtil.getNamedChildElements(targetsElement, "Target")) {
                String name = getString(target, "Name");
                parseMessages(XmlElementUtil.getFirstElementByTagName(target, "Messages"), name);
                parseModules(XmlElementUtil.getFirstElementByTagName(target, "Modules"), name);
                parseResources(XmlElementUtil.getFirstElementByTagName(target, "Resources"), name);
            }
        }
    }

    private void parseResources(final Element resources, final String parentName) {
        if (resources != null) {
            for (Element target : XmlElementUtil.getNamedChildElements(resources, "Resource")) {
                String name = getString(target, "Name");
                parseMessages(XmlElementUtil.getFirstElementByTagName(target, "Messages"), name);
            }
        }
    }

    private void parseModules(final Element modulesElement, final String parentName) {
        if (modulesElement != null) {
            for (Element module : XmlElementUtil.getNamedChildElements(modulesElement, "Module")) {
                String name = getString(module, "Name");
                parseMessages(XmlElementUtil.getFirstElementByTagName(module, "Messages"), name);
                parseNamespaces(XmlElementUtil.getFirstElementByTagName(module, "Namespaces"), name);
            }
        }
    }

    private void parseNamespaces(final Element namespacesElement, final String parentName) {
        if (namespacesElement != null) {
            for (Element namespace : XmlElementUtil.getNamedChildElements(namespacesElement,
                    "Namespace")) {
                String name = getString(namespace, "Name");

                parseMessages(XmlElementUtil.getFirstElementByTagName(namespace, "Messages"), name);
                parseTypes(XmlElementUtil.getFirstElementByTagName(namespace, "Types"), name);
            }
        }
    }

    private void parseTypes(final Element typesElement, final String parentName) {
        if (typesElement != null) {
            for (Element type : XmlElementUtil.getNamedChildElements(typesElement, "Type")) {
                String name = parentName + "." + getString(type, "Name");

                parseMessages(XmlElementUtil.getFirstElementByTagName(type, "Messages"), name);
                parseMembers(XmlElementUtil.getFirstElementByTagName(type, "Members"), name);
            }
        }
    }

    private void parseMembers(final Element membersElement, final String parentName) {
        if (membersElement != null) {
            for (Element member : XmlElementUtil.getNamedChildElements(membersElement, "Member")) {
                parseMember(member, parentName);
            }
        }
    }

    private void parseAccessors(final Element accessorsElement, final String parentName) {
        if (accessorsElement != null) {
            for (Element member : XmlElementUtil
                    .getNamedChildElements(accessorsElement, "Accessor")) {
                parseMember(member, parentName);
            }
        }
    }

    private void parseMember(final Element member, final String parentName) {
        parseMessages(XmlElementUtil.getFirstElementByTagName(member, "Messages"), parentName);
        parseAccessors(XmlElementUtil.getFirstElementByTagName(member, "Accessors"), parentName);
    }

    private void parseMessages(final Element messages, final String parentName) {
        parseMessages(messages, parentName, null);
    }

    private void parseMessages(final Element messages, final String parentName, final String subName) {
        if (messages != null) {
            for (Element message : XmlElementUtil.getNamedChildElements(messages, "Message")) {
                for (Element issue : XmlElementUtil.getNamedChildElements(message, "Issue")) {
                    parseIssue(issue, message, parentName, subName);
                }
            }
        }
    }

    private void parseIssue(final Element issue, final Element parent, final String parentName, final String subName) {
        String typeName = getString(parent, "TypeName");
        String category = getString(parent, "Category");
        String checkId = getString(parent, "CheckId");
        String issueLevel = getString(issue, "Level");

        StringBuilder msgBuilder = new StringBuilder();
        if (subName != null) {
            msgBuilder.append(subName);
            msgBuilder.append(" ");
        }
        FxCopRule rule = ruleSet.getRule(category, checkId);
        if (rule == null) {
            msgBuilder.append(typeName);
        }
        else {
            msgBuilder.append("<a href=\"");
            msgBuilder.append(rule.getUrl());
            msgBuilder.append("\">");
            msgBuilder.append(typeName);
            msgBuilder.append("</a>");
        }
        msgBuilder.append(" - ");
        msgBuilder.append(issue.getTextContent());

        String filePath = getString(issue, "Path");
        String fileName = getString(issue, "File");
        String fileLine = getString(issue, "Line");

        Warning warning = createWarning(filePath + "/" + fileName, getLineNumber(fileLine), category, msgBuilder.toString(), getPriority(issueLevel));
        if (rule != null) {
            warning.setToolTip(rule.getDescription());
        }
        warnings.add(warning);
    }

    private String getString(final Element element, final String name) {
        if (element.hasAttribute(name)) {
            return element.getAttribute(name);
        }
        else {
            return "";
        }
    }

    private Priority getPriority(final String issueLevel) {
        if (issueLevel.contains("CriticalError")) {
            return Priority.HIGH;
        }
        else if (issueLevel.contains("Error")) {
            return Priority.HIGH;
        }
        else if (issueLevel.contains("CriticalWarning")) {
            return Priority.HIGH;
        }
        else if (issueLevel.contains("Warning")) {
            return Priority.NORMAL;
        }
        else {
            return Priority.LOW;
        }
    }
}
