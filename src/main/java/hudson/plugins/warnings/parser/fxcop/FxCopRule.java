package hudson.plugins.warnings.parser.fxcop;

/**
 * Internal model for a FxCop rule.
 *
 * @author Erik Ramfelt
 */
@SuppressWarnings({"PMD", "all"})
//CHECKSTYLE:OFF
public class FxCopRule {
	private transient String name;
	private transient String typeName;
	private transient String category;
	private transient String checkId;
	private transient String url;
	private transient String description;

	public FxCopRule(final String typeName, final String category, final String checkId) {
		this.typeName = typeName;
		this.category = category;
		this.checkId = checkId;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getCategory() {
		return category;
	}

	public String getCheckId() {
		return checkId;
	}
}
