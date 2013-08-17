package hudson.plugins.warnings.parser.gendarme;

import java.net.URL;

// CHECKSTYLE:OFF
@SuppressWarnings("javadoc")
public class GendarmeRule {
	private String name;
	private String typeName;
	private GendarmeRuleType type;
	private URL url;

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(final String typeName) {
		this.typeName = typeName;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public GendarmeRuleType getType() {
		return type;
	}

	public void setType(final GendarmeRuleType type) {
		this.type = type;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(final URL url) {
		this.url = url;
	}
}
