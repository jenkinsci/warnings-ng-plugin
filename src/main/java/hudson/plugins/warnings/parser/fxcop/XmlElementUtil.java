package hudson.plugins.warnings.parser.fxcop;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

@SuppressWarnings({"PMD", "all"})
//CHECKSTYLE:OFF
public class XmlElementUtil {

	private XmlElementUtil()  {
	}

    public static List<Element> getNamedChildElements(final Element parent, final String name) {
        List<Element> elements = new ArrayList<Element>();
        if (parent != null) {
            Node child = parent.getFirstChild();
            while (child != null) {
                if ((child.getNodeType() == Node.ELEMENT_NODE) && (child.getNodeName().equals(name))) {
                    elements.add((Element) child);
                }
                child = child.getNextSibling();
            }
        }
        return elements;
    }

    public static Element getFirstElementByTagName(final Element parent, final String tagName) {
    	List<Element> foundElements = getNamedChildElements(parent, tagName);
    	if (foundElements.size() > 0) {
    		return foundElements.get(0);
    	} else {
    		return null;
    	}
    }
}
